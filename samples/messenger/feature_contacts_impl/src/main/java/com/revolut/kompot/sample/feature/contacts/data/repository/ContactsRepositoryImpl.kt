package com.revolut.kompot.sample.feature.contacts.data.repository

import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.feature.contacts.data.ContactsMapper
import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.utils.onIo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val contactsMapper: ContactsMapper
) : ContactsRepository {

    override suspend fun saveContact(contact: Contact) = onIo {
        chatDao.insertContact(contactsMapper.toEntity(contact))
    }

    override fun contactsStream(): Flow<List<Contact>> =
        chatDao.contactsStream().map { contacts ->
            contacts.map { contactsMapper.toDomain(it) }
        }.onIo()
}