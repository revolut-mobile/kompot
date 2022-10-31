package com.revolut.kompot.sample.feature.contacts.di

import com.revolut.kompot.FeatureInitialisationArgs
import com.revolut.kompot.sample.data.api.DataApi
import com.revolut.kompot.sample.feature.contacts.api.ContactsApi
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.di.AddContactFlowInjector
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di.ContactListScreenInjector
import com.revolut.kompot.sample.utils.LazySingletonHolder
import com.revolut.kompot.sample.utils.di.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    dependencies = [DataApi::class],
    modules = [ContactsFeatureModule::class]
)
interface ContactsFeatureComponent : ContactsApi, ContactListScreenInjector, AddContactFlowInjector {
    @Component.Factory
    interface Factory {
        fun create(dataApi: DataApi): ContactsFeatureComponent
    }
}

data class ContactsArguments(
    val dataApi: DataApi
) : FeatureInitialisationArgs

class ContactsApiProvider {

    companion object : LazySingletonHolder<ContactsApi, ContactsArguments>({ args ->
        DaggerContactsFeatureComponent
            .factory()
            .create(
                dataApi = args.dataApi
            )
    }) {

        internal val component: ContactsFeatureComponent get() = instance as ContactsFeatureComponent

    }

}