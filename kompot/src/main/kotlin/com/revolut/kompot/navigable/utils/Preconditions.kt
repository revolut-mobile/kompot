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

package com.revolut.kompot.navigable.utils

import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.BuildConfig
import com.revolut.kompot.common.LifecycleEvent
import timber.log.Timber

internal object Preconditions {

    @VisibleForTesting
    internal var mainThreadRequirementEnabled = true

    fun requireMainThread(context: String) {
        if (mainThreadRequirementEnabled && !Looper.getMainLooper().isCurrentThread && BuildConfig.DEBUG) {
            throw IllegalStateException("$context is only allowed on the main thread!")
        }
    }

    fun assertWrongLaunchTillHide(
        lastLifecycleEvent: LifecycleEvent?,
        methodName: String,
        createdScopeAlternative: String,
    ) {
        if (BuildConfig.DEBUG) {
            when (lastLifecycleEvent) {
                LifecycleEvent.CREATED -> Timber.e("$methodName is called before onShown, consider to use $createdScopeAlternative [$this]")
                LifecycleEvent.SHOWN -> Unit
                LifecycleEvent.HIDDEN -> error("$methodName is called after onHidden [$this]")
                LifecycleEvent.FINISHED -> error("$methodName is called after onFinished [$this]")
                null -> Timber.e("$methodName is called before onCreate [$this]")
            }
        }
    }

    fun assertWrongLaunchTillFinish(
        lastLifecycleEvent: LifecycleEvent?,
        methodName: String,
        shownScopeAlternative: String
    ) {
        if (BuildConfig.DEBUG) {
            when (lastLifecycleEvent) {
                LifecycleEvent.CREATED -> Unit
                LifecycleEvent.SHOWN -> Timber.e("$methodName is called after onShown, consider to use $shownScopeAlternative [$this]")
                LifecycleEvent.HIDDEN -> Timber.e("$methodName is called after onHidden $this]")
                LifecycleEvent.FINISHED -> error("$methodName is called after onFinished [$this]")
                null -> Timber.e("$methodName is called before onCreate [$this]")
            }
        }
    }
}