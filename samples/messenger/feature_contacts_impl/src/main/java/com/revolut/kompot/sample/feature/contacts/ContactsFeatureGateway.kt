package com.revolut.kompot.sample.feature.contacts

import com.revolut.kompot.FeatureGateway
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.di.ContactsArguments
import com.revolut.kompot.sample.feature.contacts.navigation.ContactListNavigationDestination
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListViewController

class ContactsFeatureGateway(argsProvider: () -> ContactsArguments) : FeatureGateway {

    init {
        ContactsApiProvider.init(argsProvider)
    }

    override fun getController(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller? = when (destination) {
        ContactListNavigationDestination -> ContactListViewController()
        else -> null
    }

    override fun clearReference() {
        ContactsApiProvider.clear()
    }
}