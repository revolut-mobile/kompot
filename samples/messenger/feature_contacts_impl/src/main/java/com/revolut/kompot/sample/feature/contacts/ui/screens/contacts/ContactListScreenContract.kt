package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem

interface ContactListScreenContract {

    interface ScreenModelApi : ScreenModel<UIState, IOData.EmptyOutput> {
        fun onActionClick()
    }

    data class DomainState(
        val contacts: List<Contact>
    ) : ScreenStates.Domain

    data class UIState(
        override val items: List<ListItem>
    ) : ScreenStates.UIList

}