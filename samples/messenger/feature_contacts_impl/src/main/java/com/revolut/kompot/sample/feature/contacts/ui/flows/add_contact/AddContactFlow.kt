package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.composite.stateful_flow.ModelBinding
import com.revolut.kompot.navigable.vc.composite.stateful_flow.StatefulFlowViewController
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider

class AddContactFlow : ViewController<IOData.EmptyOutput>(), StatefulFlowViewController {

    override val component by lazy(LazyThreadSafetyMode.NONE) {
        ContactsApiProvider.component
            .getAddContactFlowComponentBuilder()
            .controller(this)
            .build()
    }
    override val controllerModel by lazy(LazyThreadSafetyMode.NONE) {
        component.model
    }
    override val modelBinding by lazy(LazyThreadSafetyMode.NONE) {
        ModelBinding(controllerModel)
    }
}