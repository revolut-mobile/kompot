package com.revolut.kompot.build_first_flow.screen.input

import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.DomainState
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.InputType
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.UIState
import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

class InputStateMapper @Inject constructor() : StateMapper<DomainState, UIState> {

    override fun mapState(domainState: DomainState): UIState {
        return UIState(
            inputHint = when (domainState.inputType) {
                InputType.FIRST_NAME -> "Input first name"
                InputType.LAST_NAME -> "Input last name"
            },
            inputText = domainState.inputText
        )
    }
}