package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.modal.ModalCoordinator
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlow
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.ModelApi
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.Step
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.UIState
import javax.inject.Inject

class ContactListViewModel @Inject constructor(
    stateMapper: States.Mapper<DomainState, UIState>,
    private val repository: ContactsRepository
) : ViewControllerModel<IOData.EmptyOutput>(), ModelApi {

    override val state = ModelState(
        initialState = DomainState(emptyList()),
        stateMapper = stateMapper,
    )
    override val modalCoordinator = ModalCoordinator { step ->
        when (step) {
            is Step.AddContact -> AddContactFlow()
        }
    }

    override fun onCreated() {
        repository.contactsStream()
            .collectTillFinish { contacts ->
                state.update {
                    copy(contacts = contacts)
                }
            }
    }

    override fun onActionClick() {
        modalCoordinator.openModal(Step.AddContact)
    }

}