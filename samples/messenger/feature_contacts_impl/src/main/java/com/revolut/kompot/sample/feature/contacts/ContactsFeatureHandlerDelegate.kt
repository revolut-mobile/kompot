/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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