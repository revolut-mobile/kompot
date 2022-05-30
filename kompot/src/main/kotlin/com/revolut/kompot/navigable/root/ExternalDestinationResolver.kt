package com.revolut.kompot.navigable.root

import android.content.Context
import android.content.pm.PackageManager
import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.toIntent

class ExternalDestinationResolver(private val context: Context) {

    fun isExternalDestinationAvailable(destination: ExternalDestination): Boolean {
        return context
            .packageManager
            .resolveActivity(destination.toIntent(context), PackageManager.MATCH_DEFAULT_ONLY) != null
    }

}