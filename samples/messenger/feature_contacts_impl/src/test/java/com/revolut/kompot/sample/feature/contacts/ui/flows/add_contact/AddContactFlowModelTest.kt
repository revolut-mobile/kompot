package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.core.test.assertion.test
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.Step
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract
import org.junit.jupiter.api.Test

class AddContactFlowModelTest {

    private val contactsRepository: ContactsRepository = mock {
        onBlocking { saveContact(any()) } doReturn Unit
    }

    private var flowModel = AddContactFlowModel(contactsRepository)

    @Test
    fun `should create and save contact`() = dispatchBlockingTest {
        val firstName = "Jake"
        val lastName = "Wharton"

        val expectedContact = Contact(
            firstName = firstName,
            lastName = lastName
        )

        flowModel.test()
            .assertStep(
                step = Step.InputFirstName,
                result = InputScreenContract.OutputData(firstName)
            )
            .assertStep(
                step = Step.InputLastName,
                result = InputScreenContract.OutputData(lastName)
            ).also {
                verify(contactsRepository).saveContact(expectedContact)
            }
            .assertQuitFlow()
    }

}