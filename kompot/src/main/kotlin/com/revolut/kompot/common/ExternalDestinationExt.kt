package com.revolut.kompot.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf

internal fun ExternalDestination.toIntent(context: Context): Intent = when (this) {
    is ExternalDestination.Browser -> createBrowserIntent(url)

    is ExternalDestination.ByIntent -> intent

    is ExternalDestination.ExplicitActivity -> Intent(context, clazz).also { intent ->
        intent.flags = flags
        type?.let { intent.type = it }
    }
        .withExtras(extras)

    is ExternalDestination.ImplicitActivity -> Intent(action).also { intent ->
        intent.setDataAndType(uri, type)
        intent.flags = flags
        component?.let { intent.component = it }
    }
        .withPackageAndClassName(packageName, className)
        .withExtras(extras)
}

private fun createBrowserIntent(url: String) =
    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
        data = Uri.parse(url)
    }

private fun Intent.withExtras(extras: Map<String, Any?>?): Intent = apply {
    val pairs = extras?.entries?.map { entry -> entry.key to entry.value }?.toTypedArray() ?: emptyArray()
    putExtras(bundleOf(*pairs))
}

private fun Intent.withPackageAndClassName(packageName: String?, className: String?): Intent = apply {
    packageName?.let { pkgName ->
        className?.let { clsName ->
            this.setClassName(pkgName, clsName)
        }
    }
}