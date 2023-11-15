package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.UIState
import com.revolut.kompot.sample.ui_common.RowDelegate
import com.revolut.kompot.sample.ui_common.TextModel
import javax.inject.Inject

class ContactListStateMapper @Inject constructor() : States.Mapper<DomainState, UIState> {

    override fun mapState(domainState: DomainState): UIState =
        UIState(createContactList(domainState.contacts))

    private fun createContactList(contacts: List<Contact>) = contacts.map { contact ->
        RowDelegate.Model(
            listId = contact.id.toString(),
            title = contact.firstName,
            subtitle = TextModel(
                content = "Online",
                color = R.color.colorPrimary
            ),
            image = contact.avatar
        )
    }
}