package com.revolut.kompot.navigable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ControllerKey(val value: String) : Parcelable {
    companion object {
        fun random() = ControllerKey(UUID.randomUUID().toString())
    }
}