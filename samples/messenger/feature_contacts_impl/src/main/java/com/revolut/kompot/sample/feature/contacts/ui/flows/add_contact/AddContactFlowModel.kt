package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.common.ModelState
import com.revolut.kompot.navigable.vc.flow.FlowCoordinator
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.FlowModelApi
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.State
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.Step
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.InputType
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputViewController
import javax.inject.Inject

internal class AddContactFlowModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewControllerModel<IOData.EmptyOutput>(), FlowModelApi {

    override val state = ModelState(State())
    override val flowCoordinator = FlowCoordinator(Step.InputFirstName) { step ->
        when (step) {
            is Step.InputFirstName -> InputViewController(InputType.FIRST_NAME).withResult { output ->
                state.update { copy(firstName = output.text) }
                next(Step.InputLastName, addCurrentStepToBackStack = true)
            }
            is Step.InputLastName -> InputViewController(InputType.LAST_NAME).withResult { output ->
                val firstName = state.current.firstName.orEmpty()
                val lastName = output.text
                saveContact(firstName, lastName)
            }
        }
    }

    private fun saveContact(
        firstName: String,
        lastName: String
    ) {
        val contact = createContact(firstName, lastName)
        tillFinish {
            withLoading {
                contactsRepository.saveContact(contact)
            }
            flowCoordinator.quit()
        }
    }

    private fun createContact(
        firstName: String,
        lastName: String
    ) = Contact(
        firstName = firstName,
        lastName = lastName
    )
}