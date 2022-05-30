package com.revolut.kompot.sample.feature.chat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class MessagePreview(
    val text: String,
    val timestamp: Date
) : Parcelable