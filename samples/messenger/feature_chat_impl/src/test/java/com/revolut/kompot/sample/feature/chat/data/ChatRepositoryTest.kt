package com.revolut.kompot.sample.feature.chat.data

import com.nhaarman.mockitokotlin2.*
import com.revolut.kompot.coroutines.test.TestDispatcherExtension
import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.feature.chat.createSampleChat
import com.revolut.kompot.sample.feature.chat.createSampleChatEntity
import com.revolut.kompot.sample.feature.chat.createSampleMessage
import com.revolut.kompot.sample.feature.chat.createSampleMessageEntity
import com.revolut.kompot.sample.feature.chat.data.repository.ChatRepositoryImpl
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.utils.date.provider.MockDateProvider
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestDispatcherExtension::class)
class ChatRepositoryTest {

    private val chatDao: ChatDao = mock {
        on { chatListStream() } doReturn flowOf(
            listOf(createSampleChatEntity())
        )
    }
    private val chatMapper = ChatMapper(MockDateProvider())

    private val contactsRepository: ContactsRepository = mock()

    private val repository = createChatRepository()

    @Test
    fun `should get chat list from db`() = dispatchBlockingTest {
        repository.chatListStream()
            .testIn(this)
            .assertValues(listOf(createSampleChat()))

        verify(chatDao).chatListStream()
    }

    @Test
    fun `should save new message to db`() = dispatchBlockingTest {
        val contact = createSampleChat().contact
        val message = createSampleMessage()

        whenever(contactsRepository.saveContact(any())).thenReturn(Unit)

        val actualContactId = repository.saveMessage(contact, message)

        assertEquals(contact.id, actualContactId)

        verify(chatDao).insertMessage(any())
        verify(contactsRepository).saveContact(contact)
    }

    @Test
    fun `should receive messages from db`() = dispatchBlockingTest {
        val contactId = 1L
        val messageEntities = listOf(
            createSampleMessageEntity()
        )

        whenever(chatDao.messagesStream(any())).thenReturn(flowOf(messageEntities))

        repository
            .messagesStream(contactId)
            .testIn(this)
            .assertValues(listOf(createSampleMessage()))

        verify(chatDao).messagesStream(contactId)
    }

    @Test
    fun `should mark messages as read`() = dispatchBlockingTest {
        val contactId = 1L

        whenever(chatDao.markMessagesAsRead(any())).thenReturn(Unit)

        repository.markMessagesAsRead(contactId)

        verify(chatDao).markMessagesAsRead(contactId)
    }

    private fun createChatRepository() = ChatRepositoryImpl(
        chatDao = chatDao,
        chatMapper = chatMapper,
        contactsRepository = contactsRepository
    )

}