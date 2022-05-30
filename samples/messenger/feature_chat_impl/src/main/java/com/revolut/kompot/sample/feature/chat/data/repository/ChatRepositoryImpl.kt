package com.revolut.kompot.sample.feature.chat.data.repository

import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.feature.chat.data.ChatMapper
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.domain.Chat
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.utils.onIo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val chatMapper: ChatMapper,
    private val contactsRepository: ContactsRepository
) : ChatRepository {

    override fun chatListStream(): Flow<List<Chat>> =
        chatDao.chatListStream().map { chatItems ->
            chatItems.map { chatMapper.toDomain(it) }
        }.onIo()

    override suspend fun saveMessage(contact: Contact, message: Message): Long = onIo {
        contactsRepository.saveContact(contact)
        saveMessage(message)
        return@onIo contact.id
    }

    private suspend fun saveMessage(message: Message) =
        chatDao.insertMessage(chatMapper.toEntity(message))

    override fun messagesStream(contactId: Long): Flow<List<Message>> =
        chatDao.messagesStream(contactId).map { messageItems ->
            messageItems.map { chatMapper.toDomain(it) }
        }.onIo()

    override suspend fun markMessagesAsRead(contactId: Long) = onIo {
        chatDao.markMessagesAsRead(contactId)
    }

}