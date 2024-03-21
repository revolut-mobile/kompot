package com.revolut.kompot.sample.ui.flows.main

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.ReusableFlowStep
import com.revolut.kompot.navigable.vc.composite.ui_states_flow.UIStatesFlowModel
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.ui.views.BottomBar
import kotlinx.parcelize.Parcelize

interface MainFlowContract {

    interface FlowModelApi : UIStatesFlowModel<DomainState, UIState, Step, IOData.EmptyOutput> {
        fun onTabSelected(tabId: String)
    }

    data class DomainState(val selectedTabId: String) : States.Domain

    data class UIState(
        val selectedTabId: String,
        val tabs: List<BottomBar.Item>
    ) : States.UI

    @Parcelize
    data class State(
        val selectedTabId: String
    ) : FlowState

    sealed class Step : ReusableFlowStep {
        @Parcelize
        object ChatList : Step() {
            override val key: String get() = "chatList"
        }

        @Parcelize
        object ContactList : Step() {
            override val key: String get() = "contactList"
        }
    }
}