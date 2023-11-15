package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.core.test.assertion.applyTestDependencies
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.navigable.vc.test.testDomainStateStream
import com.revolut.kompot.sample.feature.contacts.createSampleContact
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlow
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.DomainState
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContactListViewModelTest {

    private val defaultContacts = listOf(createSampleContact())
    private val repository: ContactsRepository = mock {
        on { contactsStream() } doReturn flowOf(defaultContacts)
    }

    private val viewModel = ContactListViewModel(
        stateMapper = mock(),
        repository = repository
    ).applyTestDependencies()

    @Test
    fun `should load chat list after created`() = dispatchBlockingTest {
        val streamTest = viewModel.testDomainStateStream().testIn(this)

        viewModel.onCreated()

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
        viewModel.onCreated()
        viewModel.onActionClick()
        argumentCaptor<NavigationEvent> {
            verify(viewModel.eventsDispatcher).handleEvent(capture())
            val destination = firstValue.destination as ModalDestination.CallbackController
            assertTrue(destination.controller is AddContactFlow)
        }
    }

}