package com.revolut.kompot.navigable.utils

import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.BuildConfig

internal object Preconditions {

    @VisibleForTesting
    internal var mainThreadRequirementEnabled = true

    fun requireMainThread(context: String) {
        if (mainThreadRequirementEnabled && !Looper.getMainLooper().isCurrentThread && BuildConfig.DEBUG) {
            throw IllegalStateException("$context is only allowed on the main thread!")
        }
    }
}