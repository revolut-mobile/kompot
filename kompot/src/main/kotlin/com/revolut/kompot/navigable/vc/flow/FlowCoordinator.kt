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
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.SavedStateOwner
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.binder.StatefulModelBinder
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PushControllerCommand
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.RestorationState
import com.revolut.kompot.navigable.flow.ReusableFlowStep
import com.revolut.kompot.navigable.flow.getControllerKey
import com.revolut.kompot.navigable.vc.modal.ModalCoordinator
import kotlinx.parcelize.Parcelize

open class FlowCoordinator<STEP : FlowStep, OUTPUT : IOData.Output>(
    private val hostModel: ControllerModel,
    private val initialStep: STEP,
    private val initialBackStack: List<BackStackEntry<STEP>>,
    private val controllersFactory: FlowCoordinator<STEP, OUTPUT>.(STEP) -> Controller,
) {

    val step: STEP get() = taskState.step
    val hasBackStack: Boolean get() = _backStack.isNotEmpty()
    val restorationState: RestorationState?
        get() = when {
            savedState != null -> RestorationState.REQUIRED
            else -> null
        }

    private val modalCoordinator = ModalCoordinator<STEP, OUTPUT>(
        hostModel = hostModel,
        controllersFactory = { step ->
            controllersFactory(step)
        }
    )

    private lateinit var taskState: FlowTaskState<STEP>
    private val _backStack = mutableListOf<FlowTaskState<STEP>>()
    private val navigationCommandsBinder =
        StatefulModelBinder<FlowNavigationCommand<STEP, OUTPUT>>()

    private var savedState: Bundle? = null
    private var latestStateBackup: Backup? = null
    private lateinit var parentControllerKey: ControllerKey

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun onCreate(parentKey: ControllerKey) {
        this.parentControllerKey = parentKey
        setFlowModelState(savedState)
        navigationCommandsBinder.notify(
            PushControllerCommand(
                controller = getOrCreateCurrentStateController(),
                fromSavedState = false,
                animation = TransitionAnimation.NONE,
                backward = false,
                executeImmediately = true,
            )
        )
        modalCoordinator.performCreate()
    }

    private fun setFlowModelState(pendingSavedState: Bundle?) {
        if (pendingSavedState == null) {
            setInitialState()
        } else {
            restoreFlowModelState(pendingSavedState)
        }
    }

    private fun setInitialState() {
        taskState = FlowTaskState(
            step = initialStep,
            animation = if (initialBackStack.isEmpty()) {
                TransitionAnimation.NONE
            } else {
                initialBackStack.last().animation
            }
        )
        _backStack.clear()
        initialBackStack.forEach { (step, transition) ->
            _backStack.add(
                FlowTaskState(
                    step = step,
                    animation = transition
                )
            )
        }
    }

    private fun restoreFlowModelState(bundle: Bundle) {
        if (this::taskState.isInitialized) {
            invalidateCache(taskState, destroy = false)
        }
        bundle.classLoader = javaClass.classLoader

        if (bundle.containsKey(TASK_STATE_ARG) && bundle.containsKey(BACK_STACK_ARG)) {
            taskState = requireNotNull(bundle.getParcelable(TASK_STATE_ARG))
            setBackStack(requireNotNull(bundle.getParcelableArrayList(BACK_STACK_ARG)))
        }
    }

    private fun setBackStack(backStack: List<FlowTaskState<STEP>>) {
        _backStack.clear()
        _backStack.addAll(backStack)
    }

    internal fun next(
        step: STEP,
        addCurrentStepToBackStack: Boolean,
        animation: TransitionAnimation,
        executeImmediately: Boolean,
    ) {
        navigationCommandsBinder.notify(
            Next(
                step = step,
                addCurrentStepToBackStack = addCurrentStepToBackStack,
                animation = animation,
                executeImmediately = executeImmediately,
            )
        )
    }

    @VisibleForTesting
    fun handleBackStack(immediate: Boolean): Boolean {
        if (!hasBackStack) return false
        val backwardAnimation = taskState.animation
        val prevTaskState = taskState
        taskState = _backStack.removeLast()

        val targetController = getOrCreateCurrentStateController()
        latestStateBackup = Backup(
            state = prevTaskState,
            backstackTopEntry = taskState
        )
        navigationCommandsBinder.notify(
            PushControllerCommand(
                controller = targetController,
                fromSavedState = !targetController.created,
                animation = backwardAnimation,
                backward = true,
                executeImmediately = immediate,
            )
        )
        return true
    }

    internal fun onTransitionCanceled(backward: Boolean) {
        if (backward) {
            latestStateBackup?.let { backup ->
                taskState = backup.state
                _backStack.add(backup.backstackTopEntry)
            }
        } else {
            if (hasBackStack) {
                taskState = _backStack.removeLast()
            }
        }
    }

    @VisibleForTesting
    fun setNextState(fromCommand: Next<STEP, OUTPUT>, childState: Bundle?) {
        if (!fromCommand.addCurrentStepToBackStack) {
            invalidateCache(taskState, destroy = false)
        } else {
            _backStack.add(taskState.copy(childState = childState))
        }

        taskState = taskState.copy(
            step = fromCommand.step,
            animation = fromCommand.animation,
            childState = null,
            currentControllerKey = null
        )

        navigationCommandsBinder.notify(
            PushControllerCommand(
                controller = getOrCreateCurrentStateController(),
                fromSavedState = false,
                animation = fromCommand.animation,
                backward = false,
                executeImmediately = fromCommand.executeImmediately,
            )
        )
    }

    private fun invalidateCache(state: FlowTaskState<*>, destroy: Boolean) {
        if (state.currentControllerKey != null) {
            hostModel.controllersCache.removeController(state.currentControllerKey, destroy)
        }
    }

    private fun getOrCreateCurrentStateController(): Controller {
        val cachedController = getCachedController(parentControllerKey)
        val controller = cachedController ?: createController(taskState.step, parentControllerKey)
        if (cachedController == null && controller is SavedStateOwner) {
            controller.doOnCreate { taskState.childState?.run(controller::restoreState) }
        }

        taskState = taskState.copy(currentControllerKey = controller.key)

        return controller
    }

    private fun getCachedController(parentKey: ControllerKey): Controller? {
        val cacheKey = taskState.currentControllerKey ?: taskState.step.getReusableControllerKey(parentKey)
        return cacheKey?.let { hostModel.controllersCache.getController(cacheKey) }
    }

    private fun FlowStep.getReusableControllerKey(parentKey: ControllerKey): ControllerKey? =
        (this as? ReusableFlowStep)?.getControllerKey(parentKey)

    private fun createController(step: STEP, parentKey: ControllerKey) = controllersFactory(taskState.step).also { controller ->
        if (step is ReusableFlowStep) {
            controller.cacheStrategy = ControllerCacheStrategy.DependentOn(parentKey)
            controller.keyInitialization = { step.getControllerKey(parentKey) }
        }
    }

    @VisibleForTesting
    fun getCurrentController(): Controller = controllersFactory(taskState.step)

    internal fun quit() {
        clearBackStack()
        navigationCommandsBinder.notify(Quit())
    }

    internal fun clearBackStack() {
        if (hasBackStack) {
            _backStack.forEach { state -> invalidateCache(state, destroy = true) }
            _backStack.clear()
        }
    }

    fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT>> =
        navigationCommandsBinder

    fun saveState(outBundle: Bundle, childBundle: Bundle) {
        taskState = taskState.copy(childState = childBundle)
        outBundle.putParcelable(TASK_STATE_ARG, taskState)
        outBundle.putParcelableArrayList(BACK_STACK_ARG, ArrayList(_backStack))
        modalCoordinator.saveState(outBundle)
    }

    fun restoreState(bundle: Bundle) {
        savedState = bundle
        modalCoordinator.restoreState(bundle)
    }

    internal fun onDestroy() {
        clearBackStack()
    }

    internal fun openModal(step: STEP, style: ModalDestination.Style) = modalCoordinator.openModal(step, style)

    companion object {
        private const val TASK_STATE_ARG = "TASK_STATE_ARG"
        private const val BACK_STACK_ARG = "BACK_STACK_ARG"
    }

    private inner class Backup(
        val state: FlowTaskState<STEP>,
        val backstackTopEntry: FlowTaskState<STEP>,
    )
}

@Parcelize
internal data class FlowTaskState<S : FlowStep>(
    val step: S,
    val childState: Bundle? = null,
    val animation: TransitionAnimation = TransitionAnimation.NONE,
    val currentControllerKey: ControllerKey? = null
) : Parcelable

@Parcelize
data class BackStackEntry<S : FlowStep>(
    val step: S,
    val animation: TransitionAnimation,
) : Parcelable

fun <S : FlowStep, Out : IOData.Output, T> T.FlowCoordinator(
    initialStep: S,
    initialBackStack: List<BackStackEntry<S>> = emptyList(),
    controllersFactory: FlowCoordinator<S, Out>.(S) -> Controller,
): FlowCoordinator<S, Out> where T : FlowViewModel<S, Out>,
                                 T : ControllerModel =
    FlowCoordinator(
        hostModel = this,
        initialStep = initialStep,
        initialBackStack = initialBackStack,
        controllersFactory = controllersFactory,
    )
