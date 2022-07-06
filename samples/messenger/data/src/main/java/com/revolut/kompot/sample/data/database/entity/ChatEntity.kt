package com.revolut.kompot.sample.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import java.util.*

data class ChatEntity(

    @Embedded
    val contact: ContactEntity,

    @ColumnInfo(name = "lastMessage")
    val lastMessage: String,

    @ColumnInfo(name = "lastMessageDate")
    val lastMessageDate: Date,

    @ColumnInfo(name = "unreadCount")
    val unreadCount: Int = 0
)