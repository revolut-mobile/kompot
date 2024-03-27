package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.UIState
import com.revolut.kompot.sample.ui_common.RowDelegate
import com.revolut.kompot.sample.ui_common.TextModel
import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import com.revolut.recyclerkit.delegates.ListItem
import javax.inject.Inject

class ChatListStateMapper @Inject constructor(
    private val datePrinter: DatePrinter
) : States.Mapper<DomainState, UIState> {

    override fun mapState(domainState: DomainState): UIState {
        return UIState(items = createChatList(domainState.chats))
    }

    private fun createChatList(
        chats: List<Chat>
    ): List<ListItem> = chats.map { chat ->
        RowDelegate.Model(
            listId = chat.contact.id.toString(),
            image = chat.contact.avatar,
            title = chat.contact.firstName,
            subtitle = TextModel(chat.lastMessage.text),
            caption = datePrinter.printTime(chat.lastMessage.timestamp),
            badge = if (chat.unreadCount == 0) {
                null
            } else {
                chat.unreadCount.toString()
            },
            parcel = chat.contact
        )
    }

}