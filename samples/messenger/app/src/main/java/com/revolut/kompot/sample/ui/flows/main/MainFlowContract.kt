package com.revolut.kompot.sample.ui.flows.main

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.sample.ui.views.BottomBar
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

interface MainFlowContract {

    interface FlowModelApi : FlowModel<Step, IOData.EmptyOutput> {
        fun onTabSelected(tabId: String)
        fun tabsStateFlow(): Flow<TabsState>
    }

    data class TabsState(
        val selectedTabId: String,
        val tabs: List<BottomBar.Item>
    )

    @Parcelize
    data class State(
        val selectedTabId: String
    ) : FlowState

    sealed class Step : FlowStep {
        @Parcelize
        object ChatList : Step()

        @Parcelize
        object ContactList : Step()
    }

    companion object {
        val mainFlowKey = ControllerKey("MainFlow")
    }

}