package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesModel
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.parcelize.Parcelize

interface ChatContract {

    interface ModelApi : UIListStatesModel<DomainState, UIState, IOData.EmptyOutput> {
        fun onInputChanged(text: String)
        fun onActionButtonClick()
        fun onMessageClicked(listId: String)
    }

    data class DomainState(
        val contact: Contact,
        val messages: List<Message>,
        val messageInputText: String
    ) : States.Domain

    @Parcelize
    data class RetainedState(
        val messageInputText: String
    ) : States.PersistentDomain

    data class UIState(
        val contactName: String,
        val contactAvatar: Int,
        val contactStatus: String,
        val messageInputText: String,
        val actionButtonEnabled: Boolean,
        override val items: List<ListItem>
    ) : States.UIList

}