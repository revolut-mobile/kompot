package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlow
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract.*
import javax.inject.Inject

class ContactListScreenModel @Inject constructor(
    stateMapper: StateMapper<DomainState, UIState>,
    private val repository: ContactsRepository
) : BaseScreenModel<DomainState, UIState, IOData.EmptyOutput>(stateMapper), ScreenModelApi  {

    override val initialState = DomainState(contacts = emptyList())

    override fun onCreated() {
        super.onCreated()

        repository.contactsStream()
            .collectTillFinish { contacts ->
                updateState {
                    copy(contacts = contacts)
                }
            }
    }

    override fun onActionClick() {
        AddContactFlow().showModal()
    }

}