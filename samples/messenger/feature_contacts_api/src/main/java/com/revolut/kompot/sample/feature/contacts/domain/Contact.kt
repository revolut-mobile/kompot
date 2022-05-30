package com.revolut.kompot.sample.feature.contacts.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val avatar: Int = 0
) : Parcelable