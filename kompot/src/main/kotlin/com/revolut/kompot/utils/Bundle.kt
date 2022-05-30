package com.revolut.kompot.utils

import android.os.Bundle
import android.os.Parcel
import timber.log.Timber

fun Bundle.logSize() {
    val parcel = Parcel.obtain()
    parcel.writeValue(this)

    val bytes = parcel.marshall()
    parcel.recycle()

    Timber.tag("KompotBundleSize").i("${bytes.size}")
}