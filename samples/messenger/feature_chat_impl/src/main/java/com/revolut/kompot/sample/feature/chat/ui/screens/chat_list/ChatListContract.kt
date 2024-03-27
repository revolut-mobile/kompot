package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesModel
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem

interface ChatListContract {

    interface ModelApi : UIListStatesModel<DomainState, UIState, OutputData> {
        fun onRowClicked(contact: Contact)
    }

    data class DomainState(
        val chats: List<Chat>
    ) : States.Domain

    data class UIState(
        override val items: List<ListItem>
    ) : States.UIList

    data class OutputData(
        val selectedContact: Contact
    ) : IOData.Output

}