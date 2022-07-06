package com.revolut.kompot.sample.feature.chat.data

import com.revolut.kompot.sample.data.database.entity.ChatEntity
import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.data.database.entity.MessageEntity
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.domain.MessagePreview
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.utils.date.provider.MockDateProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChatMapperTest {

    private val dateProvider = MockDateProvider()
    private val mapper = ChatMapper(dateProvider)

    private val contactId = 4L

    private val defaultChatEntity = ChatEntity(
        contact = ContactEntity(
            id = 1,
            firstName = "Marty",
            lastName = "McFly",
            avatar = R.drawable.avatar_mc_fly
        ),
        lastMessage = "last message",
        lastMessageDate = dateProvider.provideDate(),
        unreadCount = 1
    )

    private val defaultChat = Chat(
        contact = Contact(
            id = 1,
            firstName = "Marty",
            lastName = "McFly",
            avatar = R.drawable.avatar_mc_fly
        ),
        lastMessage = MessagePreview(
            text = "last message",
            timestamp = dateProvider.provideDate()
        ),
        unreadCount = 1
    )

    private val defaultMessageEntity = MessageEntity(
        senderId = USER_ID,
        receiverId = contactId,
        message = "message",
        timestamp = dateProvider.provideDate(),
        isRead = false
    )

    private val defaultMessage = Message(
        senderId = USER_ID,
        receiverId = contactId,
        text = "message",
        timestamp = dateProvider.provideDate()
    )

    @Test
    fun `map chat entity to domain`() {
        assertEquals(defaultChat, mapper.toDomain(defaultChatEntity))
    }

    @Test
    fun `map message to entity`() {
        assertEquals(defaultMessageEntity, mapper.toEntity(defaultMessage))
    }

    @Test
    fun `map message to domain`() {

        assertEquals(defaultMessage, mapper.toDomain(defaultMessageEntity))
    }

}