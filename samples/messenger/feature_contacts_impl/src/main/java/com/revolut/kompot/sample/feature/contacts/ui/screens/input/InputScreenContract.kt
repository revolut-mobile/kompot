package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import kotlinx.parcelize.Parcelize

interface InputScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, OutputData> {
        fun onInputChanged(text: String)
        fun onActionClick()
    }
    
    @Parcelize
    data class InputData(
        val inputType: InputType
    ) : IOData.Input

    enum class InputType { FIRST_NAME, LAST_NAME }
    
    data class OutputData(
        val text: String
    ) : IOData.Output

    data class DomainState(
        val inputType: InputType,
        val inputText: String
    ) : ScreenStates.Domain

    data class UIState(
        val inputHint: String,
        val inputText: String
    ) : ScreenStates.UI
}