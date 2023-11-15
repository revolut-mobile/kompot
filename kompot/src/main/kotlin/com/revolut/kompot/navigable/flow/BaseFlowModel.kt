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

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.common.NavigationEventHandledResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.binder.StatefulModelBinder
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.vc.ViewController
import java.util.*

abstract class BaseFlowModel<STATE : FlowState, STEP : FlowStep, OUTPUT : IOData.Output> : ControllerModel(), FlowModel<STEP, OUTPUT> {
    internal lateinit var stateWrapper: FlowStateWrapper<STATE, STEP>

    private val _backStack = mutableListOf<FlowStateWrapper<STATE, STEP>>()
    internal val backStack: List<FlowStateWrapper<STATE, STEP>>
        get() = _backStack

    final override val step: STEP
        get() = stateWrapper.step

    protected val previousStep: STEP?
        get() = _backStack.lastOrNull()?.step

    final override val hasBackStack: Boolean
        get() = _backStack.isNotEmpty()

    final override val hasChildFlow: Boolean
        get() = stateWrapper.childFlowState != null

    protected var currentState: STATE
        get() = stateWrapper.state
        set(value) {
            stateWrapper = stateWrapper.copy(state = value)
        }

    protected val restorationState: RestorationState?
        get() = when {
            restorationPolicy?.postponed == true -> RestorationState.POSTPONED
            restorationPolicy != null -> RestorationState.REQUIRED
            else -> null
        }

    private var restorationPolicy: RestorationPolicy? = null

    @Deprecated("Replaced by synchronous API. See BaseFlowModel#next")
    private var onNextStateUpdater: (STATE) -> STATE? = { null }
    private var latestStateBackup: Backup? = null

    protected abstract val initialStep: STEP
    protected abstract val initialState: STATE
    protected open val initialBackStack: List<Pair<STEP, TransitionAnimation>> = emptyList()

    private val navigationCommandsBinder = StatefulModelBinder<FlowNavigationCommand<STEP, OUTPUT>>()

    final override fun onLifecycleEvent(event: LifecycleEvent) {
        if (event == LifecycleEvent.CREATED) {
            performCreate()
        }
        super.onLifecycleEvent(event)
    }

    private fun performCreate() {
        setInitialFlowModelState(restorationPolicy)
        navigationCommandsBinder.notify(
            PushControllerCommand(
                controller = getController(),
                fromSavedState = restorationState == RestorationState.REQUIRED,
                animation = TransitionAnimation.NONE,
                backward = false,
                executeImmediately = true,
            )
        )
    }

    private fun setInitialFlowModelState(restorationPolicy: RestorationPolicy?) {
        if (restorationPolicy?.postponed == true) {
            setInitialState()
            return
        }
        setFlowModelState(restorationPolicy)
    }

    private fun setFlowModelState(restorationPolicy: RestorationPolicy?) = when (restorationPolicy) {
        is RestorationPolicy.FromBundle -> restoreFlowModelState(restorationPolicy.bundle)
        is RestorationPolicy.FromParent -> restoreFlowModelState(restorationPolicy.parentFlowModel)
        null -> setInitialState()
    }

    @VisibleForTesting
    fun setInitialState() {
        stateWrapper = FlowStateWrapper(
            state = initialState,
            step = initialStep,
            animation = prepareInitialTransition()
        )
        prepareBackStack(initialBackStack)
    }

    private fun restoreFlowModelState(bundle: Bundle) {
        if (this::stateWrapper.isInitialized) {
            invalidateCache(stateWrapper, destroy = false)
        }
        bundle.classLoader = javaClass.classLoader

        if (bundle.containsKey(STATE_WRAPPER_ARG) && bundle.containsKey(BACK_STACK_ARG)) {
            stateWrapper = requireNotNull(bundle.getParcelable(STATE_WRAPPER_ARG))
            setBackStack(requireNotNull(bundle.getParcelableArrayList(BACK_STACK_ARG)))
        } else {
            setInitialState()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreFlowModelState(parentFlowModel: FlowModel<*, *>) {
        val childFlowState = (parentFlowModel as? BaseFlowModel<*, *, *>)?.stateWrapper
            ?.childFlowState

        if (childFlowState != null) {
            stateWrapper = childFlowState.stateWrapper as FlowStateWrapper<STATE, STEP>
            setBackStack(childFlowState.backStack as List<FlowStateWrapper<STATE, STEP>>)
        } else {
            setInitialState()
        }
    }

    private fun setBackStack(backStack: List<FlowStateWrapper<STATE, STEP>>) {
        _backStack.clear()
        _backStack.addAll(backStack)
    }

    final override fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT>> = navigationCommandsBinder

    @Deprecated(
        message = "Use next() command and update state with currentState = currentState.copy(...)",
        replaceWith = ReplaceWith("next(step, addCurrentStepToBackStack, animation)")
    )
    fun next(
        step: STEP,
        addCurrentStepToBackStack: Boolean,
        animation: TransitionAnimation? = null,
        executeImmediately: Boolean = false,
        stateUpdater: (STATE) -> STATE? = { null },
    ) {
        onNextStateUpdater = stateUpdater
        next(step, addCurrentStepToBackStack, animation, executeImmediately = executeImmediately)
    }

    /**
     * Start transition to the specified step
     *
     * @param executeImmediately start transition immediately without scheduling to the main thread.
     * Can produce side effects in the controller that is about to appear.
     * Problematic use case: You trigger command from the coroutine dispatched with Dispatchers.Unconfined or Dispatcher.Main.immediate.
     * If the new controller starts coroutines in the onCreated/onShown, their execution will be delayed because
     * unconfined dispatchers use event loops to prevent stack overflow in the nested invocations.
     */
    fun next(
        step: STEP,
        addCurrentStepToBackStack: Boolean,
        animation: TransitionAnimation? = null,
        executeImmediately: Boolean = false,
    ) {
        navigationCommandsBinder.notify(
            Next(
                step = step,
                addCurrentStepToBackStack = addCurrentStepToBackStack,
                animation = animation ?: TransitionAnimation.SLIDE_RIGHT_TO_LEFT,
                executeImmediately = executeImmediately,
            )
        )
    }

    fun back() {
        navigationCommandsBinder.notify(Back())
    }

    fun quitFlow() {
        clearBackStack()
        navigationCommandsBinder.notify(Quit())
    }

    fun postFlowResult(data: OUTPUT) {
        navigationCommandsBinder.notify(PostFlowResult(data))
    }

    abstract fun getController(step: STEP): Controller

    final override fun getController(): Controller {
        val cachedController = stateWrapper.currentControllerKey?.let { key -> controllersCache.getController(key) }
        val controller = cachedController ?: getController(stateWrapper.step)
        if (cachedController == null && controller is BaseScreen<*, *, *>) {
            controller.doOnCreate { stateWrapper.currentScreenState?.run(controller::restoreState) }
        }
        if (cachedController == null && controller is ViewController<*>) {
            controller.doOnCreate { stateWrapper.currentScreenState?.run(controller::restoreState) }
        }

        stateWrapper = stateWrapper.copy(currentControllerKey = controller.key)

        return controller
    }

    protected fun dependentController(
        flowKey: ControllerKey,
        step: FlowStep,
        controllerProvider: () -> Controller
    ): Controller =
        dependentController(
            flowKey = flowKey,
            controllerKey = ControllerKey(step.javaClass.canonicalName ?: step.javaClass.simpleName),
            controllerProvider = controllerProvider,
        )

    protected fun dependentController(
        flowKey: ControllerKey,
        controllerKey: ControllerKey,
        controllerProvider: () -> Controller
    ): Controller {
        val cachedController = controllersCache.getController(controllerKey)
        return cachedController ?: controllerProvider().apply {
            cacheStrategy = ControllerCacheStrategy.DependentOn(flowKey)
            keyInitialization = { controllerKey }
        }
    }

    protected fun dependentController(
        flowKey: String,
        step: FlowStep,
        controllerProvider: () -> Controller
    ): Controller = dependentController(ControllerKey(flowKey), step, controllerProvider)

    private fun prepareBackStack(initialBackStack: List<Pair<STEP, TransitionAnimation>>) {
        _backStack.clear()
        initialBackStack.forEach { (step, transition) ->
            _backStack.add(
                FlowStateWrapper(
                    state = initialState,
                    step = step,
                    animation = transition
                )
            )
        }
    }

    private fun prepareInitialTransition(): TransitionAnimation =
        if (initialBackStack.isEmpty()) {
            TransitionAnimation.NONE
        } else {
            initialBackStack.last().second
        }

    protected fun clearBackStack() {
        if (hasBackStack) {
            _backStack.forEach { state -> invalidateCache(state, destroy = true) }
            _backStack.clear()
        }
    }

    protected fun removePreviousState() {
        if (hasBackStack) {
            removePreviousStateUnsafe()
        }
    }

    protected fun restoreToStep(stepRestorationCriteria: StepRestorationCriteria): Boolean {
        _backStack.firstOrNull {
            when (stepRestorationCriteria) {
                is StepRestorationCriteria.RestoreByStep -> stepRestorationCriteria.condition(it.step)
                is StepRestorationCriteria.RestoreByClass<*> -> stepRestorationCriteria.stepClass == it.step.javaClass
            }
        }?.let {
            while (hasBackStack && _backStack.last().step != it.step) {
                removePreviousStateUnsafe()
            }
            if (stepRestorationCriteria.removeCurrent) {
                navigationCommandsBinder.notify(Back())
            }
            return true
        } ?: return false
    }

    private fun removePreviousStateUnsafe() {
        invalidateCache(_backStack.last(), destroy = true)
        _backStack.removeLast()
    }

    final override fun handleBackStack(immediate: Boolean): Boolean {
        if (!hasBackStack) return false
        val backwardAnimation = stateWrapper.animation

        val prevStateWrapper = stateWrapper
        stateWrapper = _backStack.removeLast()
        val targetController = getController()
        latestStateBackup = Backup(
            state = prevStateWrapper,
            backstackTopEntry = stateWrapper
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

    final override fun setNextState(step: STEP, animation: TransitionAnimation, addCurrentStepToBackStack: Boolean, childFlowModel: FlowModel<*, *>?) {
        if (!addCurrentStepToBackStack) {
            invalidateCache(stateWrapper, destroy = false)
        } else {
            _backStack.add(
                stateWrapper.copy(
                    childFlowState = getChildFlowState(childFlowModel)
                )
            )
        }

        stateWrapper = stateWrapper.copy(
            step = step,
            animation = animation,
            currentScreenState = null,
            childFlowState = null,
            currentControllerKey = null
        )

        onNextStateUpdater(currentState)?.let { newState ->
            currentState = newState
        }
        onNextStateUpdater = { null }
    }

    override fun onTransitionCanceled(backward: Boolean) {
        //TODO: PMNGDE-4725 Transitions started with addCurrentStepToBackStack = false are not restored
        if (backward) {
            latestStateBackup?.let { backup ->
                stateWrapper = backup.state
                _backStack.add(backup.backstackTopEntry)
            }
        } else {
            if (hasBackStack) {
                stateWrapper = _backStack.removeLast()
            }
        }
    }

    private fun invalidateCache(state: FlowStateWrapper<*, *>, destroy: Boolean) {
        if (state.currentControllerKey != null) {
            controllersCache.removeController(state.currentControllerKey, destroy)
        }
    }

    override fun updateChildFlowState(childFlowModel: FlowModel<*, *>?) {
        stateWrapper = stateWrapper.copy(childFlowState = getChildFlowState(childFlowModel))
    }

    private fun getChildFlowState(childFlowModel: FlowModel<*, *>?) =
        (childFlowModel as? BaseFlowModel<*, *, *>)?.takeIf {
            it.restorationState != RestorationState.POSTPONED
        }?.let {
            ChildFlowState(it.stateWrapper.copy(), ArrayList(it.backStack))
        }

    final override fun updateCurrentScreenState(state: Bundle) {
        stateWrapper = stateWrapper.copy(currentScreenState = state)
    }

    final override fun saveState(outState: Bundle) {
        if (restorationState == RestorationState.POSTPONED) return
        outState.putParcelable(STATE_WRAPPER_ARG, stateWrapper)
        outState.putParcelableArrayList(BACK_STACK_ARG, ArrayList(backStack))
    }

    final override fun restoreState(restorationPolicy: RestorationPolicy) {
        val postponeRestore = postponeSavedStateRestore()
        this.restorationPolicy = when (restorationPolicy) {
            is RestorationPolicy.FromBundle -> restorationPolicy.copy(postponed = postponeRestore)
            is RestorationPolicy.FromParent -> restorationPolicy.copy(postponed = postponeRestore)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun postponeSavedStateRestore() = false

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun startPostponedSavedStateRestore(): Boolean {
        val restorationPolicy = restorationPolicy?.takeIf { it.postponed } ?: return false

        setFlowModelState(restorationPolicy)

        val restorationCommand = StartPostponedStateRestore<STEP, OUTPUT>()
        navigationCommandsBinder.notify(restorationCommand)

        this.restorationPolicy = null
        return true
    }

    override fun tryHandleEvent(event: Event): EventResult? {
        if (event is NavigationEvent && handleNavigationDestination(event.destination)) {
            return NavigationEventHandledResult
        }

        return super.tryHandleEvent(event)
    }

    override fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean = false

    override fun onFinished() {
        super.onFinished()
        clearBackStack()
        this.restorationPolicy = null
    }

    private companion object {
        const val STATE_WRAPPER_ARG = "STATE_WRAPPER_ARG"
        const val BACK_STACK_ARG = "BACK_STACK_ARG"
    }

    sealed class StepRestorationCriteria(val removeCurrent: Boolean) {

        class RestoreByClass<T : FlowStep>(val stepClass: Class<T>, removeCurrent: Boolean) : StepRestorationCriteria(removeCurrent)

        class RestoreByStep(val condition: (FlowStep) -> Boolean, removeCurrent: Boolean) : StepRestorationCriteria(removeCurrent)
    }

    private inner class Backup(
        val state: FlowStateWrapper<STATE, STEP>,
        val backstackTopEntry: FlowStateWrapper<STATE, STEP>,
    )
}