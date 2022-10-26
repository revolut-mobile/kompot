package com.revolut.kompot.sample

import com.revolut.kompot.sample.data.di.DataApiProvider
import com.revolut.kompot.sample.feature.chat.ChatFeatureGateway
import com.revolut.kompot.sample.feature.chat.di.ChatArguments
import com.revolut.kompot.sample.feature.contacts.ContactsFeatureGateway
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.di.ContactsArguments
import com.revolut.kompot.sample.playground.PlaygroundFeatureGateway
import com.revolut.kompot.sample.playground.di.PlaygroundArguments
import com.revolut.kompot.sample.utils.di.CoreUtilsApiProvider

object Features {

    fun createFeatures() = listOf(
        ChatFeatureGateway {
            ChatArguments(
                dataApi = DataApiProvider.instance,
                coreUtilsApi = CoreUtilsApiProvider.instance,
                contactsApi = ContactsApiProvider.instance
            )
        },
        ContactsFeatureGateway {
            ContactsArguments(
                dataApi = DataApiProvider.instance
            )
        },
        PlaygroundFeatureGateway { PlaygroundArguments },
    )

}