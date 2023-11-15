package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIListStatesModel
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.parcelize.Parcelize

interface ContactListContract {

    interface ModelApi : ModalHostUIListStatesModel<DomainState, UIState, Step, IOData.EmptyOutput> {
        fun onActionClick()
    }

    data class DomainState(
        val contacts: List<Contact>
    ) : States.Domain

    data class UIState(
        override val items: List<ListItem>
    ) : States.UIList

    sealed interface Step : FlowStep {
        @Parcelize
        object AddContact : Step
    }
}