package com.revolut.kompot.sample

import com.revolut.kompot.FeatureHandlerDelegate
import com.revolut.kompot.sample.data.di.DataApiProvider
import com.revolut.kompot.sample.feature.chat.ChatFeatureHandlerDelegate
import com.revolut.kompot.sample.feature.chat.di.ChatArguments
import com.revolut.kompot.sample.feature.contacts.ContactsFeatureHandlerDelegate
import com.revolut.kompot.sample.feature.contacts.di.ContactsApiProvider
import com.revolut.kompot.sample.feature.contacts.di.ContactsArguments
import com.revolut.kompot.sample.playground.PlaygroundFeatureHandlerDelegate
import com.revolut.kompot.sample.playground.di.PlaygroundArguments
import com.revolut.kompot.sample.utils.di.CoreUtilsApiProvider

object Features {

    fun createFeaturesList(): List<FeatureHandlerDelegate<*, *, *>> = listOf(
        ChatFeatureHandlerDelegate {
            ChatArguments(
                dataApi = DataApiProvider.instance,
                coreUtilsApi = CoreUtilsApiProvider.instance,
                contactsApi = ContactsApiProvider.instance
            )
        },
        ContactsFeatureHandlerDelegate {
            ContactsArguments(
                dataApi = DataApiProvider.instance
            )
        },
        PlaygroundFeatureHandlerDelegate { PlaygroundArguments },
    )

}