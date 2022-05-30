package com.revolut.kompot.sample.feature.contacts

import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.feature.contacts.data.ContactsMapper
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContactsMapperTest {

    private val contact = Contact(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = 1
    )

    private val contactEntity = ContactEntity(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = 1
    )

    private val mapper = ContactsMapper()

    @Test
    fun `map contact to entity`() {
        assertEquals(contactEntity, mapper.toEntity(contact))
    }

    @Test
    fun `map entity to contact`() {
        assertEquals(contact, mapper.toDomain(contactEntity))
    }

}