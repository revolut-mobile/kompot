package com.revolut.kompot.navigable.flow.scroller

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.scroller.steps.Steps
import com.revolut.kompot.navigable.flow.scroller.steps.StepsChangeCommand
import com.revolut.kompot.navigable.flow.scroller.steps.toChangeCommand
import com.revolut.kompot.utils.MutableBehaviourFlow
import kotlinx.coroutines.flow.Flow

@ExperimentalKompotApi
abstract class BaseScrollerFlowModel<STEP : FlowStep, OUTPUT : IOData.Output> :
    ControllerModel(), ScrollerFlowModel<STEP, OUTPUT> {

    protected abstract val initialSteps: Steps<STEP>

    private val _stepsCommands by lazy(LazyThreadSafetyMode.NONE) {
        MutableBehaviourFlow<StepsChangeCommand<STEP>>().apply {
            tryEmit(initialSteps.toChangeCommand(smoothScroll = false))
        }
    }

    protected val lastStepsCommand: StepsChangeCommand<STEP>
        get() = checkNotNull(_stepsCommands.replayCache.firstOrNull()) {
            "steps must be initialised"
        }

    private val navigationCommandsBinder = ModelBinder<FlowNavigationCommand<STEP, OUTPUT>>()

    final override fun stepsCommands(): Flow<StepsChangeCommand<STEP>> = _stepsCommands

    protected fun updateSteps(
        selected: STEP? = null,
        steps: List<STEP> = lastStepsCommand.steps,
        smoothScroll: Boolean = true,
    ) {
        _stepsCommands.tryEmit(
            StepsChangeCommand(
                steps = steps,
                selected = selected,
                smoothScroll = smoothScroll
            )
        )
    }

    override fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT>> = navigationCommandsBinder

    fun back() {
        navigationCommandsBinder.notify(Back())
    }

    fun quitFlow() {
        navigationCommandsBinder.notify(Quit())
    }

    fun postFlowResult(data: OUTPUT) {
        navigationCommandsBinder.notify(PostFlowResult(data))
    }
}