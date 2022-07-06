package com.revolut.kompot.build_first_flow.screen.text

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import kotlinx.parcelize.Parcelize

interface TextScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, IOData.EmptyOutput>

    @Parcelize
    data class InputData(val text: String) : IOData.Input

    data class DomainState(
        val text: String
    ) : ScreenStates.Domain

    data class UIState(val text: String) : ScreenStates.UI
}