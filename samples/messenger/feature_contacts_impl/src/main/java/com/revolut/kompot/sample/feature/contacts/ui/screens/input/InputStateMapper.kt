package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.*
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