package com.revolut.kompot.build_first_flow.flow

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize

interface AddContactFlowContract {

    interface FlowModelApi : FlowModel<Step, IOData.EmptyOutput>

    @Parcelize
    data class State(
        val firstName: String? = null
    ) : FlowState

    sealed class Step : FlowStep {
        @Parcelize
        object InputFirstName : Step()

        @Parcelize
        object InputLastName : Step()

        @Parcelize
        data class Success(
            val firstName: String,
            val lastName: String
        ): Step()
    }
}