package com.revolut.kompot.sample.ui.flows.main

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreen
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreen
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.*
import com.revolut.kompot.sample.ui.views.BottomBar
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

private const val CHATS_TAB_ID = "CHAT_TAB_ID"
private const val CONTACTS_TAB_ID = "CONTACTS_TAB_ID"

class MainFlowModel @Inject constructor() : BaseFlowModel<State, Step, IOData.EmptyOutput>(), FlowModelApi {

    override val initialStep: Step = Step.ChatList
    override val initialState: State = State(CHATS_TAB_ID)

    override fun tabsStateFlow() = flowOf(
        TabsState(
            selectedTabId = currentState.selectedTabId,
            tabs = bottomBarTabs
        )
    )

    override fun getController(step: Step): Controller {
        val flowKey = MainFlowContract.mainFlowKey
        return when (step) {
            Step.ChatList -> dependentController(flowKey, step) {
                ChatListScreen()
            }
            is Step.ContactList -> dependentController(flowKey, step) {
                ContactListScreen()
            }
        }
    }

    override fun onTabSelected(tabId: String) {
        val tabStep = tabId.toStep()
        if (step == tabStep) {
            return
        }
        next(
            step = tabStep,
            addCurrentStepToBackStack = false,
            animation = TransitionAnimation.NONE
        )
        currentState = currentState.copy(selectedTabId = tabId)
    }

    private fun String.toStep(): Step = when (this) {
        CHATS_TAB_ID -> Step.ChatList
        CONTACTS_TAB_ID -> Step.ContactList
        else -> throw IllegalStateException("No such tab")
    }

}

private val bottomBarTabs: List<BottomBar.Item>
    get() = listOf(
        BottomBar.Item(
            id = CONTACTS_TAB_ID,
            icon = R.drawable.ic_contacts
        ),
        BottomBar.Item(
            id = CHATS_TAB_ID,
            icon = R.drawable.ic_chats
        )
    )