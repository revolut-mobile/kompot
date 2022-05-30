package com.revolut.kompot.navigable.flow.scroller.steps

import com.revolut.kompot.navigable.flow.FlowStep

internal fun <S: FlowStep> Steps<S>.toChangeCommand(smoothScroll: Boolean) =
    StepsChangeCommand(steps = steps, selected = selected, smoothScroll = smoothScroll)