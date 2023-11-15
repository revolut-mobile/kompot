package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStatesModel

interface InputContract {

    interface ModelApi : UIStatesModel<DomainState, UIState, OutputData> {
        fun onInputChanged(text: String)
        fun onActionClick()
    }

    enum class InputType { FIRST_NAME, LAST_NAME }

    data class OutputData(
        val text: String
    ) : IOData.Output

    data class DomainState(
        val inputType: InputType,
        val inputText: String
    ) : States.Domain

    data class UIState(
        val inputHint: String,
        val inputText: String
    ) : States.UI
}