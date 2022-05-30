package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts

import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract.*
import com.revolut.kompot.sample.ui_common.RowDelegate
import com.revolut.kompot.sample.ui_common.TextModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContactListStateMapperTest {

    private val contacts = listOf(
        Contact(
            id = 1,
            firstName = "Marty",
            lastName = "McFly",
            avatar = 1
        ),
        Contact(
            id = 2,
            firstName = "Marty2",
            lastName = "McFly2",
            avatar = 2
        )
    )

    private val stateMapper = ContactListStateMapper()

    @Test
    fun `create contact models`() {
        val expected = UIState(
            items = listOf(
                RowDelegate.Model(
                    listId = "1",
                    title = "Marty",
                    subtitle = TextModel("Online", R.color.colorPrimary),
                    image = 1
                ),
                RowDelegate.Model(
                    listId = "2",
                    title = "Marty2",
                    subtitle = TextModel("Online", R.color.colorPrimary),
                    image = 2
                )
            )
        )

        assertEquals(expected, stateMapper.mapState(DomainState(contacts)))
    }

}