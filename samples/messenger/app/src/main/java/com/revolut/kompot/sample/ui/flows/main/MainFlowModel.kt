package com.revolut.kompot.sample.ui.flows.main

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.flow.FlowCoordinator
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListViewController
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListViewController
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.DomainState
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.FlowModelApi
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.Step
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.UIState
import com.revolut.kompot.sample.ui.views.BottomBar
import javax.inject.Inject

private const val CHATS_TAB_ID = "CHAT_TAB_ID"
private const val CONTACTS_TAB_ID = "CONTACTS_TAB_ID"

class MainFlowModel @Inject constructor() : ViewControllerModel<IOData.EmptyOutput>(),
    FlowModelApi {

    override val state = ModelState(
        stateMapper = StateMapper(),
        initialState = DomainState(CHATS_TAB_ID)
    )
    override val flowCoordinator = FlowCoordinator(Step.ChatList) { step ->
        when (step) {
            Step.ChatList -> ChatListViewController()
            Step.ContactList -> ContactListViewController()
        }
    }

    override fun onTabSelected(tabId: String) {
        val tabStep = tabId.toStep()
        if (flowCoordinator.step == tabStep) {
            return
        }
        flowCoordinator.next(
            step = tabStep,
            addCurrentStepToBackStack = false,
            animation = TransitionAnimation.NONE,
        )
        state.update { copy(selectedTabId = tabId) }
    }

    private fun String.toStep(): Step = when (this) {
        CHATS_TAB_ID -> Step.ChatList
        CONTACTS_TAB_ID -> Step.ContactList
        else -> throw IllegalStateException("No such tab")
    }
}

class StateMapper : States.Mapper<DomainState, UIState> {
    override fun mapState(domainState: DomainState) = UIState(
        selectedTabId = domainState.selectedTabId,
        tabs = listOf(
            BottomBar.Item(
                id = CONTACTS_TAB_ID,
                icon = R.drawable.ic_contacts
            ),
            BottomBar.Item(
                id = CHATS_TAB_ID,
                icon = R.drawable.ic_chats
            )
        )
    )
}