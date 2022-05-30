package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.sample.feature.contacts.createSampleContact
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlow
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract.DomainState
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContactListScreenModelTest {

    private val defaultContacts = listOf(createSampleContact())
    private val repository: ContactsRepository = mock {
        on { contactsStream() } doReturn flowOf(defaultContacts)
    }

    private val screenModel = ContactListScreenModel(
        stateMapper = mock(),
        repository = repository
    ).apply {
        injectDependencies(mock(), mock(), mock())
    }

    @Test
    fun `should load chat list after created`() = dispatchBlockingTest {

        val streamTest = screenModel.domainStateStream().testIn(this)

        screenModel.onCreated()

        val expected = listOf(
            DomainState(
                contacts = emptyList()
            ),
            DomainState(
                contacts = defaultContacts
            )
        )

        streamTest.assertValues(expected)

        verify(repository).contactsStream()
    }

    @Test
    fun `should go to add contact when action clicked`() {
        screenModel.onCreated()
        screenModel.onActionClick()
        argumentCaptor<NavigationEvent> {
            verify(screenModel.eventsDispatcher).handleEvent(capture())
            val destination = firstValue.destination as ModalDestination.ExplicitFlow<*>
            assertTrue(destination.flow is AddContactFlow)
        }
    }

}