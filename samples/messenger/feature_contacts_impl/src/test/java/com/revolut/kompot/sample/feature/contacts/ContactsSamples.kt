package com.revolut.kompot.sample.feature.contacts

import com.revolut.kompot.sample.data.database.entity.ContactEntity
import com.revolut.kompot.sample.feature.contacts.domain.Contact

internal fun createSampleContact() = Contact(
    id = 1,
    firstName = "Marty",
    lastName = "McFly",
    avatar = 1
)

internal fun createSampleContactEntity() = ContactEntity(
    id = 1,
    firstName = "Marty",
    lastName = "McFly",
    avatar = 1
)