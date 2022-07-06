package com.revolut.kompot.sample.feature.chat

import com.revolut.kompot.sample.feature.chat.data.USER_ID
import com.revolut.kompot.sample.data.database.entity.ChatEntity
import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.data.database.entity.MessageEntity
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.domain.MessagePreview
import java.util.*

internal fun createSampleChat(
    date: Date = Date(1000L)
) = Chat(
    contact = Contact(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = R.drawable.avatar_mc_fly
    ),
    lastMessage = MessagePreview(
        text = "Some text",
        timestamp = date
    ),
    unreadCount = 1
)

internal fun createSampleChatEntity(
    date: Date = Date(1000L)
) = ChatEntity(
    contact = ContactEntity(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = R.drawable.avatar_mc_fly
    ),
    lastMessage = "Some text",
    lastMessageDate = date,
    unreadCount = 1
)

internal fun createSampleMessageEntity(
    date: Date = Date(1000L)
) = MessageEntity(
    senderId = 42,
    receiverId = USER_ID,
    message = "message",
    timestamp = date,
    isRead = false
)

internal fun createSampleMessage(
    date: Date = Date(1000L)
) = Message(
    senderId = 42,
    receiverId = USER_ID,
    text = "message",
    timestamp = date
)

internal fun createSampleContact() = Contact(
    id = 1,
    firstName = "John",
    lastName = "Newman",
    avatar = R.drawable.avatar_mc_fly
)