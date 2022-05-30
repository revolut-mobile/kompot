package com.revolut.kompot.sample.feature.contacts.data

import com.revolut.kompot.sample.feature.contacts.domain.Contact
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {

    suspend fun saveContact(contact: Contact)

    fun contactsStream(): Flow<List<Contact>>

}