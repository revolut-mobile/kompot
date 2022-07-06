package com.revolut.kompot.sample.feature.contacts.api

import com.revolut.kompot.FeatureApi
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository

interface ContactsApi : FeatureApi {

    val contactsRepository: ContactsRepository

}