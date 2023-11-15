package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.chat.domain.MessagePreview
import com.revolut.kompot.sample.ui_common.RowDelegate
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.UIState
import com.revolut.kompot.sample.ui_common.TextModel
import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ChatListStateMapperTest {

    private val defaultTime = "15:00"
    private val datePrinter: DatePrinter = mock {
        on { printTime(any()) } doReturn defaultTime
    }

    private val contact = Contact(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = R.drawable.avatar_mc_fly
    )

    private val defaultChat = Chat(
        contact = contact,
        lastMessage = MessagePreview(
            text = "Some text",
            timestamp = Date()
        ),
        unreadCount = 1
    )

    private val defaultChatDelegate = RowDelegate.Model(
        listId = "1",
        image = R.drawable.avatar_mc_fly,
        title = "Marty",
        subtitle = TextModel("Some text"),
        caption = defaultTime,
        badge = "1",
        parcel = contact
    )

    private val stateMapper = ChatListStateMapper(
        datePrinter = datePrinter
    )

    @Test
    fun `create chat models`() {
        val domainState = DomainState(
            chats = listOf(defaultChat)
        )

        val expectedState = UIState(
            items = listOf(defaultChatDelegate)
        )
        assertEquals(expectedState, stateMapper.mapState(domainState))
    }

    @Test
    fun `create do not show badge if unread count is 0`() {
        val domainState = DomainState(
            chats = listOf(defaultChat.copy(unreadCount = 0))
        )

        val expectedState = UIState(
            items = listOf(defaultChatDelegate.copy(badge = null))
        )

        assertEquals(expectedState, stateMapper.mapState(domainState))
    }

}