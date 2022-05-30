package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.*
import javax.inject.Inject

internal class InputScreenModel @Inject constructor(
    stateMapper: StateMapper<DomainState, UIState>,
    inputData: InputData
) : BaseScreenModel<DomainState, UIState, OutputData>(stateMapper), ScreenModelApi {

    override val initialState = DomainState(
        inputType = inputData.inputType,
        inputText = ""
    )

    override fun onActionClick() {
        postScreenResult(OutputData(state.inputText))
    }

    override fun onInputChanged(text: String) {
        updateState {
            copy(inputText = text)
        }
    }
}