/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.navigable.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.BuildConfig
import com.revolut.kompot.R
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.service.ScreenAddedEvent
import com.revolut.kompot.holder.ControllerViewHolder
import com.revolut.kompot.holder.DefaultControllerViewHolder
import com.revolut.kompot.holder.ModalControllerViewHolder
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.CompositeBinding
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.transition.ModalAnimatable
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.utils.logSize
import com.revolut.kompot.view.ControllerContainer
import kotlinx.coroutines.Dispatchers

@Suppress("SyntheticAccessor")
abstract class BaseFlow<STEP : FlowStep, INPUT_DATA : IOData.Input, OUTPUT_DATA : IOData.Output>(
    val inputData: INPUT_DATA
) : Controller(), Flow<OUTPUT_DATA>, EventsDispatcher {

    private val lifecycleDelegate by lazy {
        FlowLifecycleDelegate(
            controller = this,
            controllerModel = flowModel as ControllerModel,
            childControllerManagers = { childControllerManagers.values.toList() },
            onActivityResultInternal = ::onActivityResultInternal
        )
    }

    private val serviceEventHandler by lazy {
        FlowServiceEventHandler(
            controller = this,
            controllerModel = flowModel as ControllerModel,
            parentController = parentController,
            parentControllerManager = parentControllerManager,
            view = view
        )
    }

    private val navActionsScheduler: NavActionsScheduler
        get() = findRootFlow().navActionsScheduler

    final override var onFlowResult: (data: OUTPUT_DATA) -> Unit = { }

    override val layoutId: Int
        get() = parentControllerManager.defaultFlowLayout ?: R.layout.base_flow_container

    @VisibleForTesting
    internal lateinit var childManagerContainerView: ViewGroup
    private val mainControllerManager: ControllerManager by lazy {
        getChildControllerManager(childManagerContainerView)
    }
    private val childControllerManagers = LinkedHashMap<String, ControllerManager>()

    internal lateinit var currentController: Controller

    protected abstract val flowModel: FlowModel<STEP, OUTPUT_DATA>

    override val controllerDelegates by lazy {
        component.getControllerExtensions()
    }
    private val tillDestroyBinding = CompositeBinding()

    @IdRes
    protected open val containerId: Int = R.id.container

    override fun createView(inflater: LayoutInflater): View {
        val name = this::class.java.simpleName
        val view = patchLayoutInflaterWithTheme(inflater).inflate(layoutId, null, false) as? ControllerContainer
            ?: throw IllegalStateException("$name: root ViewGroup should be ControllerContainer")

        view.applyEdgeToEdgeConfig()
        this.view = view as View
        childManagerContainerView = (view as View).findViewById(containerId)
            ?: throw IllegalStateException("$name: container for child manager should be presented")

        view.tag = controllerName
        return view
    }

    open fun getModalAnimatable(): ((context: Context) -> ModalAnimatable)? {
        return null
    }

    private fun getControllerViewHolder(container: ViewGroup, modal: Boolean): ControllerViewHolder {
        val modalAnimatable = getModalAnimatable()
        return if (modal && modalAnimatable != null)
            ModalControllerViewHolder(container, modalAnimatable)
        else
            DefaultControllerViewHolder(container)
    }

    internal open fun getChildControllerManager(container: ViewGroup, extraKey: String = ""): ControllerManager {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("container.id is not present")
        }
        val modal = container != childManagerContainerView
        return childControllerManagers.getOrPut("${container.id}_$extraKey") {
            ControllerManager(
                modal = modal,
                defaultFlowLayout = parentControllerManager.defaultFlowLayout,
                controllersCache = controllersCache,
                controllerViewHolder = getControllerViewHolder(container, modal),
                onAttachController = ::onChildControllerAttached,
                onDetachController = ::onChildControllerDetached,
            ).apply {
                hooksProvider = parentControllerManager.hooksProvider
            }
        }
    }

    @CallSuper
    internal open fun onChildControllerAttached(controller: Controller, controllerManager: ControllerManager) {
        if (controllerManager != mainControllerManager && (controllerManager.activeController == null || controllerManager.activeController == controller)) {
            childControllerManagers.values.forEach { childControllerManager ->
                if (childControllerManager != controllerManager && childControllerManager.activeController != null) {
                    childControllerManager.onDetach()
                }
            }
        }
    }

    @CallSuper
    internal open fun onChildControllerDetached(controller: Controller, controllerManager: ControllerManager) {
        if (controllerManager != mainControllerManager && (controllerManager.activeController == null || controllerManager.activeController == controller)) {
            childControllerManagers.values.reversed().forEach { childControllerManager ->
                if (childControllerManager != controllerManager && childControllerManager.activeController != null) {
                    if (controllerManager.attached) {
                        childControllerManager.onAttach()
                    }
                    return //we need only one active controller manager after a current
                }
            }
        }
    }

    final override fun onCreate() {
        super.onCreate()
        (flowModel as ControllerModel).injectDependencies(
            dialogDisplayer = findRootFlow().rootDialogDisplayer,
            eventsDispatcher = this,
            controllersCache = controllersCache,
            mainDispatcher = Dispatchers.Main.immediate
        )

        onCreateFlowView(view)
        lifecycleDelegate.onCreate()
        pushControllerNow(restore = flowModel.restorationNeeded)

        tillDestroyBinding += flowModel.navigationBinder()
            .bind(::processFlowNavigationCommand)
    }

    final override fun onDestroy() {
        childControllerManagers.values.forEach { manager -> manager.onDestroy() }
        super.onDestroy()
        tillDestroyBinding.clear()
        navActionsScheduler.cancel(key.value)
        onDestroyFlowView()
        lifecycleDelegate.onDestroy()
    }

    private fun pushControllerNow(
        controller: Controller = flowModel.getController(),
        animation: TransitionAnimation = TransitionAnimation.NONE,
        backward: Boolean = false,
        restore: Boolean = false
    ) {
        if (restore && controller is BaseFlow<*, *, *>) {
            controller.doOnCreate {
                controller.restoreState(RestorationPolicy.FromParent(flowModel))
            }
        }

        currentController = controller
        mainControllerManager.show(currentController, animation, backward, this)

        if (currentController is BaseScreen<*, *, *>) {
            handleEvent(ScreenAddedEvent(currentController, this, animation != TransitionAnimation.NONE))
        }

        updateUi()
    }

    private fun pushController(
        controller: Controller = flowModel.getController(),
        animation: TransitionAnimation = TransitionAnimation.NONE,
        backward: Boolean = false,
        restore: Boolean = false
    ) = scheduleNavAction {
        pushControllerNow(controller, animation, backward, restore)
    }

    override fun onAttach() {
        super.onAttach()
        updateUi()
        lifecycleDelegate.onAttach()
        childControllerManagers.values.reversed().any { it.onAttach() }
    }

    override fun onDetach() {
        super.onDetach()
        lifecycleDelegate.onDetach()
    }

    override fun onTransitionStart(enter: Boolean) {
        super.onTransitionStart(enter)
        lifecycleDelegate.onTransitionStart(enter)
    }

    override fun onTransitionEnd(enter: Boolean) {
        super.onTransitionEnd(enter)
        lifecycleDelegate.onTransitionEnd(enter)
    }

    override fun onHostPaused() {
        super.onHostPaused()
        lifecycleDelegate.onHostPaused()
    }

    override fun onHostResumed() {
        super.onHostResumed()
        lifecycleDelegate.onHostResumed()
    }

    final override fun onHostStarted() {
        super.onHostStarted()
        lifecycleDelegate.onHostStarted()
    }

    final override fun onHostStopped() {
        super.onHostStopped()
        lifecycleDelegate.onHostStopped()
    }

    open fun onActivityResultInternal(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifecycleDelegate.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        lifecycleDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun handleEvent(event: Event): EventResult? = serviceEventHandler.handleEvent(event)

    protected open fun onCreateFlowView(view: View) = Unit

    protected open fun onDestroyFlowView() = Unit

    private fun updateUi() = updateUi(flowModel.step)

    protected abstract fun updateUi(step: STEP)

    internal fun back() {
        if (!handleBack()) {
            (parentController as? BaseFlow<*, *, *>)?.back()
        }
    }

    final override fun handleBack(): Boolean = handleFlowBack() != BackHandleResult.UNHANDLED

    internal open fun handleFlowBack(): BackHandleResult {
        for (manager in childControllerManagers.values.reversed()) {
            if (manager.handleBack()) {
                return BackHandleResult.INTERCEPTED
            }
        }

        if (!backEnabled) {
            return BackHandleResult.INTERCEPTED
        }

        if (handleBackStack()) {
            return BackHandleResult.POP_BACK_STACK
        }

        return if (super.handleBack()) BackHandleResult.INTERCEPTED else BackHandleResult.UNHANDLED
    }

    override fun handleQuit() {
        if (!handleBackStack()) {
            quitFlow(navActionsScheduler)
        }
    }

    final override fun onParentManagerCleared() {
        childControllerManagers.values.reversed().forEach {
            it.activeController?.onParentManagerCleared()
        }
        super.onParentManagerCleared()
    }

    private fun next(command: Next<STEP, OUTPUT_DATA>) {
        if (command.addCurrentStepToBackStack) {
            updateChildFlowState()
        } else if (currentController is BaseScreen<*, *, *>) {
            flowModel.updateCurrentScreenState(Bundle())
        }

        flowModel.setNextState(command.step, command.animation, command.addCurrentStepToBackStack, (currentController as? BaseFlow<*, *, *>)?.flowModel)
        pushController(animation = command.animation, backward = false)
    }

    private fun handleBackStack(): Boolean {
        if (flowModel.hasBackStack) {
            val backwardAnimation = flowModel.animation
            flowModel.restorePreviousState()

            val targetController = flowModel.getController()
            pushController(
                controller = targetController,
                animation = backwardAnimation,
                backward = true,
                restore = !targetController.created
            )
            return true
        }
        return false
    }

    private fun updateChildFlowState() {
        currentController.let { controller ->
            if (controller is BaseScreen<*, *, *>) {
                flowModel.updateCurrentScreenState(controller.saveState())
            }

            controller as? BaseFlow<*, *, *>
        }?.also { childFlow ->
            childFlow.updateChildFlowState()
            flowModel.updateChildFlowState(childFlow.flowModel)
        }
    }

    internal fun saveState(outState: Bundle) {
        updateChildFlowState()
        flowModel.saveState(outState)
        if (BuildConfig.DEBUG) {
            outState.logSize()
        }
    }

    internal fun restoreState(restorationPolicy: RestorationPolicy) {
        flowModel.restoreState(restorationPolicy)
    }

    private fun processFlowNavigationCommand(command: FlowNavigationCommand<STEP, OUTPUT_DATA>) {
        when (command) {
            is Next -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("BaseFlowModel.next()")
                next(command)
            }
            is Back -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("BaseFlowModel.back()")
                back()
            }
            is Quit -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("BaseFlowModel.quit()")
                quitFlow(navActionsScheduler)
            }
            is PostFlowResult -> {
                Preconditions.requireMainThread("BaseFlowModel.postFlowResult()")
                onFlowResult(command.data)
            }
            is StartPostponedStateRestore -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("BaseFlowModel.startPostponedSavedStateRestore()")
                pushController(restore = true)
            }
        }
    }

    internal fun getFlowModel(): FlowModel<STEP, OUTPUT_DATA> {
        return flowModel
    }

    private fun scheduleNavAction(action: () -> Unit) {
        navActionsScheduler.schedule(key.value, action)
    }

    internal enum class BackHandleResult { INTERCEPTED, POP_BACK_STACK, UNHANDLED }
}

internal fun NavActionsScheduler.ensureAvailability(newCommand: FlowNavigationCommand<*, *>): Boolean {
    if (hasPendingActions()) {
        if (BuildConfig.DEBUG) {
            error(IllegalStateException("Can't start $newCommand. Kompot can only handle one command at a time."))
        } else {
            return false
        }
    }
    return true
}

internal fun Controller.quitFlow(navActionsScheduler: NavActionsScheduler) {
    if (parentControllerManager.modal) {
        navActionsScheduler.schedule(key.value) { parentControllerManager.clear() }
    } else {
        parentController?.handleQuit()
    }
}