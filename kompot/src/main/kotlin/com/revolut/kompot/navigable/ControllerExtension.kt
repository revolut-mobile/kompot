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

package com.revolut.kompot.navigable

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import com.revolut.kompot.navigable.utils.collectTillDetachView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

abstract class ControllerExtension {

    private lateinit var attachedScope: CoroutineScope
    private var attached = false
    private var detached = false

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun init(attachedScope: CoroutineScope) {
        this.attachedScope = attachedScope
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun onParentLifecycleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START -> {
                onCreate()
            }
            Lifecycle.Event.ON_RESUME -> {
                attached = true
                detached = false
                onAttach()
            }
            Lifecycle.Event.ON_PAUSE -> {
                attached = false
                detached = true
                onDetach()
            }
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY -> {
                onDestroy()
            }
            Lifecycle.Event.ON_ANY -> Unit
        }
    }

    open fun onCreate() = Unit

    open fun onDestroy() = Unit

    open fun onAttach() = Unit

    open fun onDetach() = Unit

    open fun handleBack(): Boolean = false

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit

    protected fun <T> Flow<T>.collectTillDetachView(
        onError: suspend (Throwable) -> Unit = { Timber.e(it) },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job = collectTillDetachView(
        attached = attached,
        detached = detached,
        attachedScope = attachedScope,
        onError = onError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach
    )
}