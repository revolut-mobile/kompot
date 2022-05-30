package com.revolut.kompot.sample.feature.contacts

import com.revolut.kompot.DestinationHandlingResult
import com.revolut.kompot.FeatureFlowStep
import com.revolut.kompot.FeatureHandlerDelegate
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.feature.contacts.api.ContactsApi
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.di.ContactsArguments
import com.revolut.kompot.sample.feature.contacts.navigation.ContactListNavigationDestination
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreen
import kotlinx.parcelize.Parcelize

class ContactsFeatureHandlerDelegate(
    argsProvider: () -> ContactsArguments
) : FeatureHandlerDelegate<ContactsArguments, ContactsApi, ContactsFeatureFlowStep>(argsProvider) {

    init {
        ContactsApiProvider.init(argsProvider)
    }

    override fun canHandleFeatureFlowStep(featureStep: FeatureFlowStep): Boolean = featureStep is ContactsFeatureFlowStep

    override fun getController(step: ContactsFeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller = when(step) {
        ContactsFeatureFlowStep.Contacts -> ContactListScreen()
    }

    override fun handleDestination(destination: NavigationDestination): DestinationHandlingResult? = when(destination) {
        ContactListNavigationDestination -> DestinationHandlingResult(ContactsFeatureFlowStep.Contacts)
        else -> null
    }

    override fun clearReference() {
        ContactsApiProvider.clear()
    }

}

sealed class ContactsFeatureFlowStep : FeatureFlowStep {
    @Parcelize
    object Contacts : ContactsFeatureFlowStep()
}