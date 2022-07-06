package com.revolut.kompot.sample.playground.flows.scroller

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlowModel
import kotlinx.parcelize.Parcelize

interface DemoScrollerFlowContract {

    interface FlowModelApi : ScrollerFlowModel<Step, IOData.EmptyOutput>

    sealed class Step : FlowStep {
        @Parcelize
        object FirstStep : Step()

        @Parcelize
        object SecondStep : Step()

        @Parcelize
        object ThirdStep : Step()
    }
}