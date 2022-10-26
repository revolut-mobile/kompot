package com.revolut.kompot.sample.feature.contacts

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.coroutines.test.TestDispatcherExtension
import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.feature.contacts.data.ContactsMapper
import com.revolut.kompot.sample.feature.contacts.data.repository.ContactsRepositoryImpl
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestDispatcherExtension::class)
class ContactsRepositoryTest {

    private val chatDao: ChatDao = mock {
        on { contactsStream() } doReturn flowOf(
            listOf(createSampleContactEntity())
        )
    }

    private val repository = ContactsRepositoryImpl(
        chatDao = chatDao,
        contactsMapper = ContactsMapper()
    )

    @Test
    fun `should get contacts from db`() = dispatchBlockingTest {

        repository
            .contactsStream()
            .testIn(this)
            .assertValues(listOf(createSampleContact()))

        verify(chatDao).contactsStream()
    }

    @Test
    fun `should save contact`() = dispatchBlockingTest {
        val contact = createSampleContact()

        repository.saveContact(contact)

        verify(chatDao).insertContact(any())
    }

}