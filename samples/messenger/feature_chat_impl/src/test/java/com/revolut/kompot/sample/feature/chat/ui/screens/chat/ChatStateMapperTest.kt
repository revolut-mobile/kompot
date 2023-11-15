package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.data.USER_ID
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.ui.delegates.MessageRowDelegate
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.UIState
import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.*

class ChatStateMapperTest {

    private val contact = Contact(
        id = 1,
        firstName = "John",
        lastName = "Newman",
        avatar = R.drawable.avatar_mc_fly
    )

    private val defaultDomainState = DomainState(
        contact = contact,
        messageInputText = "hello",
        messages = listOf()
    )
    private val defaultUIState = UIState(
        contactName = contact.firstName,
        contactAvatar = contact.avatar,
        contactStatus = "Online",
        messageInputText = "hello",
        actionButtonEnabled = true,
        items = listOf()
    )

    private val defaultTime = "15:00"
    private val datePrinter: DatePrinter = mock {
        on { printTime(any()) } doReturn defaultTime
    }

    private val stateMapper = ChatStateMapper(
        datePrinter = datePrinter
    )

    @Test
    fun `should map domain state with message`() {
        assertEquals(defaultUIState, stateMapper.mapState(defaultDomainState))
    }

    @Test
    fun `should disable action button if input is empty`() {
        val domainState = defaultDomainState.copy(
            messageInputText = ""
        )

        assertFalse(stateMapper.mapState(domainState).actionButtonEnabled)
    }

    @Test
    fun `should map messages`() {
        val contactId = 1L
        val messages = listOf(
            Message(
                receiverId = USER_ID,
                senderId = contactId,
                text = "message1",
                timestamp = Date()
            ),
            Message(
                receiverId = contactId,
                senderId = USER_ID,
                text = "message2",
                timestamp = Date()
            )
        )

        val domainState = defaultDomainState.copy(
            messages = messages
        )

        val expected = defaultUIState.copy(
            items = listOf(
                MessageRowDelegate.Model(
                    listId = "0",
                    text = "message1",
                    caption = defaultTime,
                    gravity = MessageRowDelegate.Model.Gravity.START,
                    background = MessageRowDelegate.Model.Background.DARK
                ),
                MessageRowDelegate.Model(
                    listId = "1",
                    text = "message2",
                    caption = defaultTime,
                    gravity = MessageRowDelegate.Model.Gravity.END,
                    background = MessageRowDelegate.Model.Background.DEFAULT
                )
            )
        )

        assertEquals(expected, stateMapper.mapState(domainState))
    }

}