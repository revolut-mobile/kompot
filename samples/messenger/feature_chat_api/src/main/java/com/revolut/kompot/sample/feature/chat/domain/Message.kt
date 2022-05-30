package com.revolut.kompot.sample.feature.chat.domain

import com.revolut.kompot.sample.feature.chat.data.USER_ID
import java.util.*

data class Message(
    val senderId: Long,
    val receiverId: Long,
    val text: String,
    val timestamp: Date
) {

    companion object {

        fun createToUser(
            contactId: Long,
            text: String,
            date: Date
        ) = Message(
            senderId = contactId,
            receiverId = USER_ID,
            text = text,
            timestamp = date
        )

        fun createFromUser(
            contactId: Long,
            text: String,
            date: Date
        ) = Message(
            senderId = USER_ID,
            receiverId = contactId,
            text = text,
            timestamp = date
        )

    }

}