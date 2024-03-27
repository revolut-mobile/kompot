package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.InputType
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.UIState
import javax.inject.Inject

class InputStateMapper @Inject constructor() : States.Mapper<DomainState, UIState> {

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