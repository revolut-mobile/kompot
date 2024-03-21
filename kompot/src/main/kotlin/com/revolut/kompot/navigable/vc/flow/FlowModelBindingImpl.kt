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

package com.revolut.kompot.navigable.vc.flow

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.holder.ControllerViewHolder
import com.revolut.kompot.holder.DefaultControllerViewHolder
import com.revolut.kompot.holder.ModalControllerViewHolder
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.SavedStateOwner
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.flow.ControllerManagersHolder
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PushControllerCommand
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.ensureAvailability
import com.revolut.kompot.navigable.flow.quitFlow
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.transition.BackwardTransitionOwner
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.parent.ParentControllerModelBindingDelegate
import com.revolut.kompot.view.ControllerContainer

internal class FlowModelBindingImpl<M : FlowViewModel<S, Out>, S : FlowStep, Out : IOData.Output>(
    private val containerId: Int,
    private val controller: FlowViewController,
    val model: M,
    private val onStepUpdated: ((step: S) -> Unit)? = null,
    private val childControllerManagers: ControllerManagersHolder = ControllerManagersHolder(),
    private val parentControllerModelBindingDelegate: ParentControllerModelBindingDelegate = ParentControllerModelBindingDelegate(
        childControllerManagersProvider = childControllerManagers,
        controller = controller,
    )
) : FlowModelBinding, ModelBinding by parentControllerModelBindingDelegate {

    private val viewController: ViewController<*> get() = controller as ViewController<*>

    private val navActionsScheduler: NavActionsScheduler
        get() = viewController.findRootFlow().navActionsScheduler
    private val controllersCache: ControllersCache
        get() = viewController.controllersCache

    @VisibleForTesting
    internal var mainControllerContainer: ControllerContainer? = null
    override val hasBackStack: Boolean
        get() = model.flowCoordinator.hasBackStack

    override fun onCreate() {
        model.flowCoordinator.onCreate(viewController.key)
        viewController.tillDestroyBinding += model.flowCoordinator.navigationBinder()
            .bind(::processFlowNavigationCommand)

        (viewController.view as? BackwardTransitionOwner)?.doOnBackwardEvent {
            //using immediate because backward transition have already started
            handleBackStack(immediate = true)
        }
    }

    override fun onDestroy() {
        model.flowCoordinator.onDestroy()
        childControllerManagers.all.forEach { manager -> manager.onDestroy() }
        navActionsScheduler.cancel(viewController.key.value)
    }

    override fun handleBack(defaultHandler: () -> Boolean): Boolean {
        if (parentControllerModelBindingDelegate.handleBack(defaultHandler)) {
            return true
        }

        return handleBackStack()
    }

    private fun handleBackStack(immediate: Boolean = false) =
        model.flowCoordinator.handleBackStack(immediate)

    private fun next(command: Next<S, Out>) {
        val bundle = if (command.addCurrentStepToBackStack) {
            Bundle().also { bundle ->
                (requireCurrentController() as? SavedStateOwner)?.saveState(bundle)
            }
        } else {
            null
        }
        model.flowCoordinator.setNextState(command, bundle)
    }

    override fun handleQuit(): Boolean {
        if (!handleBackStack()) {
            viewController.quitFlow(navActionsScheduler)
        }
        return true
    }

    private fun pushControllerNow(
        controller: Controller,
        animation: TransitionAnimation = TransitionAnimation.NONE,
        backward: Boolean = false,
    ) {
        val container = mainControllerContainer ?: initMainControllerManager()
        val controllerManager = getOrCreateChildControllerManager(
            controllerContainer = container,
            id = container.containerId
        )
        controllerManager.show(controller, animation, backward, viewController)
        onStepUpdated?.invoke(model.flowCoordinator.step)
    }

    private fun pushController(command: PushControllerCommand<S, Out>) {
        fun push() {
            pushControllerNow(
                controller = command.controller,
                animation = command.animation,
                backward = command.backward,
            )
        }
        if (command.executeImmediately) {
            push()
        } else {
            scheduleNavAction { push() }
        }
    }

    private fun initMainControllerManager(): ControllerContainer {
        val mainContainerView = viewController.view.findViewById<View>(containerId)
        requireNotNull(mainContainerView) { "${viewController.controllerName}: container for child manager should be presented" }
        require(mainContainerView is ControllerContainer) { "${viewController.controllerName}: container for child manager should be ControllerContainer" }
        mainContainerView.containerId = ControllerContainer.MAIN_CONTAINER_ID
        mainControllerContainer = mainContainerView
        return mainContainerView
    }

    @VisibleForTesting
    internal fun getOrCreateChildControllerManager(
        controllerContainer: ControllerContainer,
        id: String,
    ): ControllerManager {
        val containerId = controllerContainer.containerId
        if (containerId == ControllerContainer.NO_CONTAINER_ID) {
            throw IllegalStateException("containerId should be set for child controller containers")
        }
        val modal = controllerContainer.containerId == ControllerContainer.MODAL_CONTAINER_ID
        return childControllerManagers.getOrAdd(id) {
            ControllerManager(
                modal = modal,
                defaultControllerContainer = viewController.parentControllerManager.defaultControllerContainer,
                controllersCache = controllersCache,
                controllerViewHolder = getControllerViewHolder(
                    controllerContainer as ViewGroup,
                    modal
                ),
                onAttachController = ::onChildControllerAttached,
                onDetachController = ::onChildControllerDetached,
                onTransitionCanceled = ::onTransitionCanceled,
            ).apply {
                hooksProvider = viewController.parentControllerManager.hooksProvider
            }
        }
    }

    private fun onTransitionCanceled(backward: Boolean) {
        model.flowCoordinator.onTransitionCanceled(backward)
    }

    private fun onChildControllerAttached(controller: Controller, controllerManager: ControllerManager) {
        moveModalToLifecycleForeground(controller, controllerManager)
    }

    private fun onChildControllerDetached(controller: Controller, controllerManager: ControllerManager) {
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

    private fun getControllerViewHolder(
        container: ViewGroup,
        modal: Boolean
    ): ControllerViewHolder {
        val modalAnimatable = viewController.findRootFlow().getModalAnimatable()
        return if (modal && modalAnimatable != null)
            ModalControllerViewHolder(container, modalAnimatable)
        else
            DefaultControllerViewHolder(container)
    }

    private fun processFlowNavigationCommand(command: FlowNavigationCommand<S, Out>) {
        when (command) {
            is Next -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("FlowCoordinator.next()")
                next(command)
            }

            is PushControllerCommand -> {
                Preconditions.requireMainThread("Push controller")
                pushController(command)
            }

            is Quit -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("FlowCoordinator#quit()")
                viewController.quitFlow(navActionsScheduler)
            }

            else -> error("$command is not supported")
        }
    }

    override fun saveState(outState: Bundle) {
        val childBundle = Bundle()
        (requireCurrentController() as? SavedStateOwner)?.saveState(childBundle)
        model.flowCoordinator.saveState(outState, childBundle)
    }

    private fun requireCurrentController(): Controller {
        val controllerContainer = checkNotNull(mainControllerContainer)
        val controllerManager = childControllerManagers.get(controllerContainer.containerId)
        return checkNotNull(controllerManager?.activeController)
    }

    override fun restoreState(state: Bundle) {
        model.flowCoordinator.restoreState(state)
    }

    private fun scheduleNavAction(action: () -> Unit) {
        navActionsScheduler.schedule(viewController.key.value, action)
    }
}

@Suppress("FunctionName")
fun <M : FlowViewModel<S, Out>, S : FlowStep, Out : IOData.Output> FlowViewController.ModelBinding(
    model: M,
    containerId: Int = R.id.container,
    onStepUpdated: ((step: S) -> Unit)? = null
): FlowModelBinding {
    return FlowModelBindingImpl(containerId, this, model, onStepUpdated)
}