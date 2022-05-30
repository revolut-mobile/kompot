package com.revolut.kompot.sample.playground.flows.demo

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize

interface DemoFlowContract {

    interface FlowModelApi : FlowModel<Step, IOData.EmptyOutput>

    @Parcelize
    class State : FlowState

    sealed class Step : FlowStep {
        @Parcelize
        object Step1 : Step()

        @Parcelize
        object Step2: Step()
    }

}