package com.revolut.kompot.sample.feature.chat.data

import com.revolut.kompot.sample.data.database.entity.ChatEntity
import com.revolut.kompot.sample.data.database.entity.MessageEntity
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.domain.MessagePreview
import com.revolut.kompot.sample.utils.date.provider.DateProvider
import javax.inject.Inject

class ChatMapper @Inject constructor(
    private val dateProvider: DateProvider
) {

    fun toDomain(chatEntity: ChatEntity): Chat = with(chatEntity) {
        Chat(
            contact = Contact(
                id = contact.id,
                firstName = contact.firstName,
                lastName = contact.lastName,
                avatar = contact.avatar
            ),
            lastMessage = MessagePreview(
                text = lastMessage,
                timestamp = lastMessageDate
            ),
            unreadCount = unreadCount
        )
    }

    fun toEntity(message: Message): MessageEntity {
        return MessageEntity(
            senderId = message.senderId,
            receiverId = message.receiverId,
            message = message.text,
            timestamp = dateProvider.provideDate(),
            isRead = false
        )
    }

    fun toDomain(messageEntity: MessageEntity): Message = with(messageEntity) {
        Message(
            senderId = senderId,
            receiverId = receiverId,
            text = message,
            timestamp = timestamp
        )
    }

}