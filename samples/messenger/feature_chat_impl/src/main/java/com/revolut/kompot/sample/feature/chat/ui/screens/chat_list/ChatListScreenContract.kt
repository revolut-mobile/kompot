package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem

interface ChatListScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, OutputData> {
        fun onRowClicked(contact: Contact)
    }

    data class DomainState(
        val chats: List<Chat>
    ) : ScreenStates.Domain

    data class UIState(
        override val items: List<ListItem>
    ) : ScreenStates.UIList

    data class OutputData(
        val selectedContact: Contact
    ): IOData.Output

}