package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.*
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreen
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.InputData
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.InputType
import javax.inject.Inject

internal class AddContactFlowModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : BaseFlowModel<State, Step, IOData.EmptyOutput>(), FlowModelApi {

    override val initialStep = Step.InputFirstName
    override val initialState = State()

    override fun getController(step: Step): Controller = when (step) {
        is Step.InputFirstName -> InputScreen(InputData(InputType.FIRST_NAME)).onResult { output ->
            currentState = currentState.copy(firstName = output.text)
            next(Step.InputLastName, addCurrentStepToBackStack = true)
        }
        is Step.InputLastName -> InputScreen(InputData(InputType.LAST_NAME)).onResult { output ->
            val firstName = currentState.firstName.orEmpty()
            val lastName = output.text
            saveContact(firstName, lastName)
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
            quitFlow()
        }
    }

    private fun createContact(
        firstName: String,
        lastName: String
    ) = Contact(
        firstName = firstName,
        lastName = lastName
    )

    //todo: probably add to kompot
    private fun <O : IOData.Output> BaseScreen<*, *, O>.onResult(block: (O) -> Unit) = apply { onScreenResult = block }

}