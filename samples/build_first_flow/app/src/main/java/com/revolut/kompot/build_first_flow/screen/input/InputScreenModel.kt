package com.revolut.kompot.build_first_flow.screen.input

import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.*
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
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