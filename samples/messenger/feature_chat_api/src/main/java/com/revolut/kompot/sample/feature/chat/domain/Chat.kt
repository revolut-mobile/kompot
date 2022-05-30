package com.revolut.kompot.sample.feature.chat.domain

import android.os.Parcelable
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    val contact: Contact,
    val lastMessage: MessagePreview,
    val unreadCount: Int
) : Parcelable