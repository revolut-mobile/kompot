package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.parcelize.Parcelize

interface ChatScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, IOData.EmptyOutput> {
        fun onInputChanged(text: String)
        fun onActionButtonClick()
    }

    data class DomainState(
        val contact: Contact,
        val messages: List<Message>,
        val messageInputText: String
    ) : ScreenStates.Domain

    @Parcelize
    data class RetainedState(
        val messageInputText: String
    ): ScreenStates.RetainedDomain

    data class UIState(
        val contactName: String,
        val contactAvatar: Int,
        val contactStatus: String,
        val messageInputText: String,
        val actionButtonEnabled: Boolean,
        override val items: List<ListItem>
    ) : ScreenStates.UIList

}