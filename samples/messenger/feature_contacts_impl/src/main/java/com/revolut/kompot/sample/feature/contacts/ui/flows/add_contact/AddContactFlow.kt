package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract.Step
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.di.AddContactFlowComponent

class AddContactFlow : BaseFlow<Step, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val component: AddContactFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        ContactsApiProvider.component
            .getAddContactFlowComponentBuilder()
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) { component.flowModel }

    override fun updateUi(step: Step) = Unit
}