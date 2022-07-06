package com.revolut.kompot.sample.playground.screens.demo

import android.graphics.Color
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.parcelize.Parcelize

interface DemoScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, OutputData> {
        fun onAction(id: String)
    }

    @Parcelize
    data class InputData(
        val title: String,
        val counter: Int,
        val highlighted: Boolean = false,
        val color: Int = Color.MAGENTA
    ) : IOData.Input

    data class OutputData(
        val value: Int
    ) : IOData.Output

    data class DomainState(
        val value: Int = 0
    ) : ScreenStates.Domain

    data class UIState(
        override val items: List<ListItem>
    ) : ScreenStates.UIList
}