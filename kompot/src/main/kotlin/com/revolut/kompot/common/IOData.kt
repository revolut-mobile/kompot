package com.revolut.kompot.common

import android.os.Parcelable
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize

interface IOData {
    interface Input : Parcelable

    interface Output

    @Parcelize
    object EmptyInput : Input

    object EmptyOutput : Output

    companion object {
        const val INPUT_BUNDLE_ARG = "INPUT_BUNDLE_ARG"
    }
}

fun IOData.Input.toBundle() = bundleOf(IOData.INPUT_BUNDLE_ARG to this)