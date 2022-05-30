package com.revolut.kompot.sample.feature.chat.data

import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun chatListStream(): Flow<List<Chat>>

    suspend fun saveMessage(contact: Contact, message: Message): Long

    fun messagesStream(contactId: Long): Flow<List<Message>>

    suspend fun markMessagesAsRead(contactId: Long)

}