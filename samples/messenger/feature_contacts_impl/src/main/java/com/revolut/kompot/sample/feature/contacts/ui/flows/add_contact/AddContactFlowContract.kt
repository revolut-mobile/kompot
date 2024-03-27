package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.revolut.kompot.common.IOData

import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.composite.stateful_flow.StatefulFlowModel
import kotlinx.parcelize.Parcelize

interface AddContactFlowContract {

    interface FlowModelApi : StatefulFlowModel<State, Step, IOData.EmptyOutput>

    @Parcelize
    data class State(
        val firstName: String? = null
    ) : FlowState

    sealed class Step : FlowStep {
        @Parcelize
        object InputFirstName : Step()
        @Parcelize
        object InputLastName : Step()
    }
}