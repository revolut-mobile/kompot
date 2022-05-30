package com.revolut.kompot.sample.feature.contacts.data

import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import javax.inject.Inject

class ContactsMapper @Inject constructor() {

    fun toEntity(contact: Contact): ContactEntity {
        return with(contact) {
            ContactEntity(
                id = id,
                firstName = firstName,
                lastName = lastName,
                avatar = avatar
            )
        }
    }

    fun toDomain(contactEntity: ContactEntity): Contact {
        return with(contactEntity) {
            Contact(
                id = id,
                firstName = firstName,
                lastName = lastName,
                avatar = avatar
            )
        }
    }

}