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
import com.revolut.kompot.KompotPlugin
import com.revolut.kompot.R
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.service.ScreenAddedEvent
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.holder.ControllerViewHolder
import com.revolut.kompot.holder.DefaultControllerViewHolder
import com.revolut.kompot.holder.ModalControllerViewHolder
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.SavedStateOwner
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.hooks.LifecycleViewTagHook
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.transition.BackwardTransitionOwner
import com.revolut.kompot.navigable.transition.ModalAnimatable
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.utils.logSize
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.ControllerContainer.Companion.MAIN_CONTAINER_ID
import com.revolut.kompot.view.ControllerContainer.Companion.MODAL_CONTAINER_ID
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

@Suppress("SyntheticAccessor")
abstract class BaseFlow<STEP : FlowStep, INPUT_DATA : IOData.Input, OUTPUT_DATA : IOData.Output>(
    val inputData: INPUT_DATA
) : Controller(), Flow<OUTPUT_DATA>, EventsDispatcher, SavedStateOwner {

    private val lifecycleDelegate by lazy {
        FlowLifecycleDelegate(
            controller = this,
            controllerModel = flowModel as ControllerModel,
            childControllerManagers = { childControllerManagers.all },
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
        get() = parentControllerManager.defaultControllerContainer ?: R.layout.base_flow_container

    @VisibleForTesting
    internal lateinit var mainControllerContainer: ControllerContainer
    private val childControllerManagers = ControllerManagersHolder()

    protected abstract val flowModel: FlowModel<STEP, OUTPUT_DATA>
    override val hasBackStack: Boolean get() = flowModel.hasBackStack

    abstract override val component: BaseFlowComponent

    override val controllerExtensions by lazy {
        component.getControllerExtensions()
    }

    @IdRes
    protected open val containerId: Int = R.id.container

    override fun createView(inflater: LayoutInflater): View {
        val inflatedLayout = getViewInflater(inflater).inflate(layoutId, null, false)
        require(inflatedLayout is ControllerContainer) { "$controllerName: root ViewGroup should be ControllerContainer" }
        inflatedLayout.applyEdgeToEdgeConfig()
        inflatedLayout.tag = this.controllerName
        hooksProvider?.getHook(LifecycleViewTagHook.Key)?.tagId?.let { lifecycleTag ->
            inflatedLayout.setTag(lifecycleTag, lifecycle)
        }

        val mainContainerView = inflatedLayout.findViewById<View>(containerId)
        requireNotNull(mainContainerView) { "$controllerName: container for child manager should be presented" }
        require(mainContainerView is ControllerContainer) { "$controllerName: container for child manager should be ControllerContainer" }
        mainContainerView.containerId = MAIN_CONTAINER_ID
        mainControllerContainer = mainContainerView

        return inflatedLayout.also { this.view = it }
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

    internal fun getOrCreateChildControllerManager(controllerContainer: ControllerContainer, id: String): ControllerManager {
        val containerId = controllerContainer.containerId
        if (containerId == ControllerContainer.NO_CONTAINER_ID) {
            throw IllegalStateException("containerId should be set for child controller containers")
        }
        val modal = controllerContainer.containerId == MODAL_CONTAINER_ID
        return childControllerManagers.getOrAdd(id) {
            ControllerManager(
                modal = modal,
                defaultControllerContainer = parentControllerManager.defaultControllerContainer,
                controllersCache = controllersCache,
                controllerViewHolder = getControllerViewHolder(controllerContainer as ViewGroup, modal),
                onAttachController = ::onChildControllerAttached,
                onDetachController = ::onChildControllerDetached,
                onTransitionCanceled = ::onChildControllerManagerTransitionCanceled,
            ).apply {
                hooksProvider = parentControllerManager.hooksProvider
            }
        }
    }

    private fun onChildControllerManagerTransitionCanceled(backward: Boolean) {
        flowModel.onTransitionCanceled(backward)
    }

    @CallSuper
    internal open fun onChildControllerAttached(controller: Controller, controllerManager: ControllerManager) {
        moveModalToLifecycleForeground(controller, controllerManager)
    }

    @CallSuper
    internal open fun onChildControllerDetached(controller: Controller, controllerManager: ControllerManager) {
        removeModalFromLifecycleForeground(controller, controllerManager)
    }

    /**
     * Mark every controller under a modal as detached
     */
    private fun moveModalToLifecycleForeground(controller: Controller, controllerManager: ControllerManager) {
        if (controllerManager.modal && (controllerManager.activeController == null || controllerManager.activeController == controller)) {
            childControllerManagers.all.forEach { childControllerManager ->
                if (childControllerManager != controllerManager && childControllerManager.activeController != null) {
                    childControllerManager.onDetach()
                }
            }
        }
    }

    /**
     * Bring back attached state for the controllers under a modal
     */
    private fun removeModalFromLifecycleForeground(controller: Controller, controllerManager: ControllerManager) {
        if (controllerManager.modal && (controllerManager.activeController == null || controllerManager.activeController == controller)) {
            childControllerManagers.all.asReversed().forEach { childControllerManager ->
                if (childControllerManager != controllerManager && childControllerManager.activeController != null) {
                    if (controllerManager.attached) {
                        childControllerManager.onAttach()
                    }
                    if (childControllerManager.modal) {
                        return //we need only one active modal controller manager after a current
                    }
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
            mainDispatcher = Dispatchers.Main.immediate,
            controllerModelExtensions = component.getControllerModelExtensions(),
        )

        onCreateFlowView(view)
        lifecycleDelegate.onCreate()

        tillDestroyBinding += flowModel.navigationBinder()
            .bind(::processFlowNavigationCommand)

        (view as? BackwardTransitionOwner)?.doOnBackwardEvent {
            //using immediate because backward transition have already started
            if (!handleBackStack(immediate = true)) {
                quitFlow(navActionsScheduler)
            }
        }
    }

    final override fun onDestroy() {
        childControllerManagers.all.forEach { manager -> manager.onDestroy() }
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
        restore: Boolean = false,
    ) {
        if (restore && controller is BaseFlow<*, *, *>) {
            controller.doOnCreate {
                controller.restoreState(RestorationPolicy.FromParent(flowModel))
            }
        }

        val controllerManager = getOrCreateChildControllerManager(
            controllerContainer = mainControllerContainer,
            id = mainControllerContainer.containerId
        )
        controllerManager.show(controller, animation, backward, this)

        if (controller is BaseScreen<*, *, *>) {
            handleEvent(ScreenAddedEvent(controller, this, animation != TransitionAnimation.NONE))
        }

        updateUi()
    }

    private fun pushController(command: PushControllerCommand<STEP, OUTPUT_DATA>) {
        fun push() {
            pushControllerNow(
                controller = command.controller,
                animation = command.animation,
                backward = command.backward,
                restore = command.fromSavedState,
            )
        }
        if (command.executeImmediately) {
            push()
        } else {
            scheduleNavAction { push() }
        }
    }

    override fun onAttach() {
        super.onAttach()
        updateUi()
        lifecycleDelegate.onAttach()

        for (manager in childControllerManagers.all.asReversed()) {
            if (manager.activeController != null) {
                manager.onAttach()
                if (manager.modal) break
            }
        }
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback -> callback.onControllerAttached(this) }
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

    override fun onTransitionCanceled() {
        super.onTransitionCanceled()
        lifecycleDelegate.onTransitionCanceled()
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
        for (manager in childControllerManagers.all.asReversed()) {
            if (manager.handleBack()) {
                return BackHandleResult.INTERCEPTED
            }
        }

        if (super.handleBack()) {
            return BackHandleResult.INTERCEPTED
        }

        if (handleBackStack()) {
            return BackHandleResult.POP_BACK_STACK
        }

        return BackHandleResult.UNHANDLED
    }

    override fun handleQuit() {
        if (!handleBackStack()) {
            quitFlow(navActionsScheduler)
        }
    }

    final override fun onParentManagerCleared() {
        childControllerManagers.all.asReversed().forEach {
            it.activeController?.onParentManagerCleared()
        }
        super.onParentManagerCleared()
    }

    private fun next(command: Next<STEP, OUTPUT_DATA>) {
        val currentController = requireCurrentController()
        if (command.addCurrentStepToBackStack) {
            updateChildFlowState()
        }

        flowModel.setNextState(command.step, command.animation, command.addCurrentStepToBackStack, (currentController as? BaseFlow<*, *, *>)?.flowModel)

        pushController(
            command = PushControllerCommand(
                controller = flowModel.getController(),
                animation = command.animation,
                backward = false,
                fromSavedState = false,
                executeImmediately = command.executeImmediately,
            )
        )
    }

    private fun handleBackStack(immediate: Boolean = false) = flowModel.handleBackStack(immediate)

    private fun updateChildFlowState() {
        when (val currentController = requireCurrentController()) {
            is BaseFlow<*, *, *> -> {
                currentController.updateChildFlowState()
                flowModel.updateChildFlowState(currentController.flowModel)
            }

            is SavedStateOwner -> {
                val bundle = Bundle()
                currentController.saveState(outState = bundle)
                flowModel.updateCurrentScreenState(bundle)
            }
        }
    }

    final override fun saveState(outState: Bundle) {
        updateChildFlowState()
        flowModel.saveState(outState)
        if (BuildConfig.DEBUG) {
            outState.logSize()
        }
    }

    internal fun restoreState(restorationPolicy: RestorationPolicy) {
        flowModel.restoreState(restorationPolicy)
    }

    final override fun restoreState(state: Bundle) {
        flowModel.restoreState(RestorationPolicy.FromBundle(state))
    }

    private fun processFlowNavigationCommand(command: FlowNavigationCommand<STEP, OUTPUT_DATA>) {
        when (command) {
            is Next -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("BaseFlowModel.next()")
                next(command)
                logCommand(command)
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
                pushController(
                    command = PushControllerCommand.immediate(
                        controller = flowModel.getController(),
                        fromSavedState = true
                    )
                )
            }

            is PushControllerCommand -> {
                Preconditions.requireMainThread("Push controller")
                pushController(command)
            }
        }
    }

    private fun requireCurrentController(): Controller {
        val controllerManager = childControllerManagers.get(mainControllerContainer.containerId)
        return checkNotNull(controllerManager?.activeController)
    }

    private fun logCommand(command: FlowNavigationCommand<STEP, OUTPUT_DATA>) {
        if (BuildConfig.DEBUG) {
            Timber.d("Kompot NAVIGATION TO $command")
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

internal fun NavActionsScheduler.ensureAvailability(newCommand: Any): Boolean {
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
        //If there is no parentController, we'll let receiver controller to handle quit itself
        //because it must be a root flow
        (parentController ?: this).handleQuit()
    }
}

internal val ControllerManager.containerId get() = (controllerViewHolder.container as ControllerContainer).containerId