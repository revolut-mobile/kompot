package com.revolut.kompot.sample.ui.flows.root

import com.revolut.kompot.FeatureFlowStep
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize

interface RootFlowContract {
    interface FlowModelApi : FlowModel<Step, IOData.EmptyOutput>

    @Parcelize
    class State : FlowState

    sealed class Step : FlowStep {
        @Parcelize
        object MainFlow : Step()

        @Parcelize
        data class FeatureManagerStep(val featureFlowStep: FeatureFlowStep) : Step()
    }
}