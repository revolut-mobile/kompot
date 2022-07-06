package com.revolut.kompot.build_first_screen.screen

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import kotlinx.parcelize.Parcelize

interface DemoScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, IOData.EmptyOutput>

    @Parcelize
    data class InputData(val title: String) : IOData.Input

    data class DomainState(
        val title: String
    ) : ScreenStates.Domain

    data class UIState(val title: String) : ScreenStates.UI
}