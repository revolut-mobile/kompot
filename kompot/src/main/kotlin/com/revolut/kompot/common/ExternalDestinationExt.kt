/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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