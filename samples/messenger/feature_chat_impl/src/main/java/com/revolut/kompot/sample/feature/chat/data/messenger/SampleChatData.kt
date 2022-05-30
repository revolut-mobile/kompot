package com.revolut.kompot.sample.feature.chat.data.messenger

import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.contacts.domain.Contact

val contacts = listOf(
    Contact(
        id = 1,
        firstName = "Marty",
        lastName = "McFly",
        avatar = R.drawable.avatar_mc_fly
    ),
    Contact(
        id = 2,
        firstName = "Emmett",
        lastName = "Brown",
        avatar = R.drawable.avatar_dr_brown
    ),
    Contact(
        id = 3,
        firstName = "Biff",
        lastName = "Tannen",
        avatar = R.drawable.avatar_biff
    )
)

val messagePhrases = listOf(
    "Hey", "Hello", "It's time", "Wazzup", "Howdy",
    "How are you doing?", "What time is now?", "Call me maybe",
    "It's me", "Send memes", "I'll be there by 10", "Let's meet"
)