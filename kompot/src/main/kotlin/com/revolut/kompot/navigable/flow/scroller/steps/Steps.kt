package com.revolut.kompot.navigable.flow.scroller.steps

import com.revolut.kompot.navigable.flow.FlowStep

data class Steps<S : FlowStep>(
    val steps: List<S>,
    val selected: S = requireNotNull(steps.firstOrNull()) { "Non empty list should be provided for steps" }
) {

    companion object {
        operator fun <S : FlowStep> invoke(vararg steps: S): Steps<S> = Steps(steps = steps.toList())
    }
}

data class StepsChangeCommand<S : FlowStep>(
    val steps: List<S>,
    val selected: S?,
    val smoothScroll: Boolean,
)