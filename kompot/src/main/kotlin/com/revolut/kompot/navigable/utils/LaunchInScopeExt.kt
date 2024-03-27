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

import com.revolut.kompot.BuildConfig
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.ErrorInterceptedEventResult
import com.revolut.kompot.common.ErrorInterceptionEvent
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.LifecycleEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

internal fun <T> Flow<T>.collectTillDetachView(
    attached: Boolean,
    detached: Boolean,
    attachedScope: CoroutineScope,
    onError: suspend (Throwable) -> Unit = { Timber.e(it) },
    onSuccessCompletion: suspend () -> Unit = {},
    onEach: suspend (T) -> Unit = {}
): Job {
    if (BuildConfig.DEBUG) {
        when {
            detached -> error("collectTillDetachView is called after onDetach [$this]")
            !attached -> error("collectTillDetachView is called before onAttach [$this]")
        }
    }
    return launchInScope(
        scope = attachedScope,
        onError = onError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach
    )
}

internal fun <T> Flow<T>.collectTillHide(
    eventsDispatcher: EventsDispatcher,
    lastLifecycleEvent: LifecycleEvent?,
    shownScope: CoroutineScope,
    handleError: suspend (Throwable) -> Boolean,
    onSuccessCompletion: suspend () -> Unit,
    onEach: suspend (T) -> Unit,
): Job {
    Preconditions.assertWrongLaunchTillHide(
        lastLifecycleEvent = lastLifecycleEvent,
        methodName = "collectTillHide",
        createdScopeAlternative = "collectTillFinish"
    )
    return launchInScope(eventsDispatcher, shownScope, handleError, onSuccessCompletion, onEach)
}

internal fun <T> Flow<T>.collectTillFinish(
    eventsDispatcher: EventsDispatcher,
    lastLifecycleEvent: LifecycleEvent?,
    createdScope: CoroutineScope,
    handleError: suspend (Throwable) -> Boolean,
    onSuccessCompletion: suspend () -> Unit,
    onEach: suspend (T) -> Unit,
): Job {
    Preconditions.assertWrongLaunchTillFinish(
        lastLifecycleEvent = lastLifecycleEvent,
        methodName = "collectTillFinish",
        shownScopeAlternative = "collectTillHide"
    )
    return launchInScope(eventsDispatcher, createdScope, handleError, onSuccessCompletion, onEach)
}

internal fun <T> tillFinish(
    eventsDispatcher: EventsDispatcher,
    createdScope: CoroutineScope,
    lastLifecycleEvent: LifecycleEvent?,
    handleError: (Throwable) -> Boolean = { false },
    block: suspend CoroutineScope.() -> T
): Job {
    Preconditions.assertWrongLaunchTillFinish(
        lastLifecycleEvent = lastLifecycleEvent,
        methodName = "tillFinish",
        shownScopeAlternative = "tillHide"
    )

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        if (dispatchErrorInterception(eventsDispatcher, exception)){
            return@CoroutineExceptionHandler
        }

        if (!handleError(exception)) {
            sendErrorEvent(eventsDispatcher, exception)
        }
    }

    return createdScope.launch(exceptionHandler) {
        block()
    }
}

internal fun <T> tillHide(
    eventsDispatcher: EventsDispatcher,
    shownScope: CoroutineScope,
    lastLifecycleEvent: LifecycleEvent?,
    handleError: (Throwable) -> Boolean = { false },
    block: suspend CoroutineScope.() -> T
): Job {
    Preconditions.assertWrongLaunchTillHide(
        lastLifecycleEvent = lastLifecycleEvent,
        methodName = "tillHide",
        createdScopeAlternative = "tillFinish"
    )

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        if (dispatchErrorInterception(eventsDispatcher, exception)){
            return@CoroutineExceptionHandler
        }

        if (!handleError(exception)) {
            sendErrorEvent(eventsDispatcher, exception)
        }
    }

    return shownScope.launch(exceptionHandler) {
        block()
    }
}

private fun <T> Flow<T>.launchInScope(
    scope: CoroutineScope,
    onError: suspend (Throwable) -> Unit = { Timber.e(it) },
    onSuccessCompletion: suspend () -> Unit = {},
    onEach: suspend (T) -> Unit = {}
): Job =
    onEach(onEach)
        .onCompletion { cause ->
            if (cause == null) onSuccessCompletion()
        }
        .catch { cause ->
            onError(cause)
        }
        .launchIn(scope)

private fun <T> Flow<T>.launchInScope(
    eventsDispatcher: EventsDispatcher,
    scope: CoroutineScope,
    handleError: suspend (Throwable) -> Boolean = { false },
    onSuccessCompletion: suspend () -> Unit = {},
    onEach: suspend (T) -> Unit = {}
): Job =
    onEach(onEach)
        .onCompletion { cause ->
            if (cause == null) onSuccessCompletion()
        }
        .catch { cause ->
            if (dispatchErrorInterception(eventsDispatcher, cause)){
                return@catch
            }
            if (!handleError(cause)) {
                sendErrorEvent(eventsDispatcher, cause)
            }
        }
        .launchIn(scope)

private fun sendErrorEvent(eventsDispatcher: EventsDispatcher, throwable: Throwable) {
    eventsDispatcher.handleEvent(ErrorEvent(throwable))
}

private fun dispatchErrorInterception(eventsDispatcher: EventsDispatcher, throwable: Throwable): Boolean {
    val result = eventsDispatcher.handleEvent(ErrorInterceptionEvent(throwable))
    return result == ErrorInterceptedEventResult
}