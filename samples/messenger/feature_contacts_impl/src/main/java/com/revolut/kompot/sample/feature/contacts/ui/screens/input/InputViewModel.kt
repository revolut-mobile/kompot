package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.ModelApi
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.OutputData
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.UIState
import javax.inject.Inject

internal class InputViewModel @Inject constructor(
    stateMapper: States.Mapper<DomainState, UIState>,
    inputType: InputContract.InputType
) : ViewControllerModel<OutputData>(), ModelApi {

    override val state = ModelState(
        initialState = DomainState(
            inputType = inputType,
            inputText = ""
        ),
        stateMapper = stateMapper,
    )

    override fun onActionClick() {
        postResult(OutputData(state.current.inputText))
    }

    override fun onInputChanged(text: String) {
        state.update {
            copy(inputText = text)
        }
    }
}