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

import android.os.SystemClock
import androidx.annotation.CallSuper
import com.revolut.kompot.BuildConfig
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.ErrorEventResult
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.InternalDestination
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.handleNavigationEvent
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.utils.single_task.IllegalConcurrentAccessException
import com.revolut.kompot.navigable.utils.single_task.SingleTasksRegistry
import com.revolut.kompot.utils.ContextId
import com.revolut.kompot.utils.ContextId.CreatedScopeContextId
import com.revolut.kompot.utils.ContextId.ShownScopeContextId
import com.revolut.kompot.utils.ControllerScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class ControllerModel {

    private var _mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
    val mainDispatcher: CoroutineDispatcher
        get() = _mainDispatcher

    private lateinit var _dialogDisplayer: DialogDisplayer
    val dialogDisplayer: DialogDisplayer
        get() = _dialogDisplayer

    private lateinit var _eventsDispatcher: EventsDispatcher
    val eventsDispatcher: EventsDispatcher
        get() = _eventsDispatcher

    private lateinit var _controllersCache: ControllersCache
    val controllersCache: ControllersCache
        get() = _controllersCache

    private var _createdScope = ModelScope(CreatedScopeContextId)
    internal val createdScope: CoroutineScope
        get() = _createdScope
    private var _shownScope = ModelScope(ShownScopeContextId)
    internal val shownScope: CoroutineScope
        get() = _shownScope

    private var hideTime = 0L
    private var _lastLifecycleEvent: LifecycleEvent? = null
    protected val lastLifecycleEvent: LifecycleEvent?
        get() = _lastLifecycleEvent

    private val singleTasksRegistry = SingleTasksRegistry()

    open fun injectDependencies(
        dialogDisplayer: DialogDisplayer,
        eventsDispatcher: EventsDispatcher,
        controllersCache: ControllersCache,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
    ) {
        _dialogDisplayer = dialogDisplayer
        _eventsDispatcher = eventsDispatcher
        _controllersCache = controllersCache
        _mainDispatcher = mainDispatcher

        _shownScope += mainDispatcher
        _createdScope += mainDispatcher
    }

    open fun setBlockingLoadingVisibility(visible: Boolean, immediate: Boolean = false) {
        if (visible) {
            dialogDisplayer.showLoadingDialog(if (immediate) 0 else 1000)
        } else {
            dialogDisplayer.hideLoadingDialog()
        }
    }

    @CallSuper
    open fun onLifecycleEvent(event: LifecycleEvent) {
        _lastLifecycleEvent = event
        when (event) {
            LifecycleEvent.SHOWN -> {
                onShown(if (hideTime > 0L) (SystemClock.elapsedRealtime() - hideTime) else 0L)
            }
            LifecycleEvent.HIDDEN -> {
                hideTime = SystemClock.elapsedRealtime()
                shownScope.coroutineContext.cancelChildren()
                onHiddenCleanUp()
                onHidden()
            }
            LifecycleEvent.CREATED -> {
                onCreated()
            }
            LifecycleEvent.FINISHED -> {
                createdScope.coroutineContext.cancelChildren()
                onFinishedCleanUp()
                onFinished()
            }
        }
    }

    open fun onHiddenCleanUp() = Unit

    open fun onFinishedCleanUp() = Unit

    open fun onShown(idleTimeMs: Long) = Unit

    open fun onHidden() = Unit

    open fun onCreated() = Unit

    open fun onFinished() = Unit

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.collectTillHide(
        handleError: suspend (Throwable) -> Boolean = { false },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job {
        assertWrongLaunchTillHide(
            methodName = "collectTillHide",
            createdScopeAlternative = "collectTillFinish"
        )
        return launchInScope(shownScope, handleError, onSuccessCompletion, onEach)
    }

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.collectTillFinish(
        handleError: suspend (Throwable) -> Boolean = { false },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job {
        assertWrongLaunchTillFinish(
            methodName = "collectTillFinish",
            shownScopeAlternative = "collectTillHide"
        )
        return launchInScope(createdScope, handleError, onSuccessCompletion, onEach)
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.launchInScope(
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
                if (!handleError(cause)) {
                    sendErrorEvent(cause)
                }
            }
            .launchIn(scope)

    protected fun <T> tillFinish(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job {
        assertWrongLaunchTillFinish(
            methodName = "tillFinish",
            shownScopeAlternative = "tillHide"
        )

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            if (!handleError(exception)) {
                sendErrorEvent(exception)
            }
        }

        return createdScope.launch(exceptionHandler) {
            block()
        }
    }

    protected fun <T> tillHide(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job {
        assertWrongLaunchTillHide(
            methodName = "tillHide",
            createdScopeAlternative = "tillFinish"
        )

        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            if (!handleError(exception)) {
                sendErrorEvent(exception)
            }
        }

        return shownScope.launch(exceptionHandler) {
            block()
        }
    }

    protected suspend fun <T> withLoading(block: suspend () -> T): T = withContext(mainDispatcher) {
        try {
            setBlockingLoadingVisibility(true)
            block()
        } finally {
            setBlockingLoadingVisibility(false)
        }
    }

    @Suppress("FunctionName")
    private fun ModelScope(contextId: ContextId): CoroutineScope =
        ControllerScope() + mainDispatcher + contextId

    private fun sendErrorEvent(throwable: Throwable) {
        eventsDispatcher.handleEvent(ErrorEvent(throwable))
    }

    private fun assertWrongLaunchTillFinish(
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

    private fun assertWrongLaunchTillHide(
        methodName: String,
        createdScopeAlternative: String
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

    open fun handleErrorEvent(throwable: Throwable): Boolean = false

    open fun tryHandleEvent(event: Event): EventResult? {
        if (event is ErrorEvent && handleErrorEvent(event.throwable)) {
            return ErrorEventResult(true)
        }

        return null
    }

    fun navigate(internalDestination: InternalDestination<*>) = internalDestination.navigate()

    fun NavigationDestination.navigate() = eventsDispatcher.handleNavigationEvent(this)

    fun <T : IOData.Output> Screen<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN,
        onResult: ((T) -> Unit)? = null
    ) =
        eventsDispatcher.handleNavigationEvent(
            ModalDestination.ExplicitScreen(
                screen = this,
                onResult = onResult,
                style = style
            )
        )

    fun <T : IOData.Output> Flow<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN,
        onResult: ((T) -> Unit)? = null
    ) =
        eventsDispatcher.handleNavigationEvent(
            ModalDestination.ExplicitFlow(
                flow = this,
                onResult = onResult,
                style = style
            )
        )

    fun <Result : DialogModelResult> showDialog(dialogModel: DialogModel<Result>): kotlinx.coroutines.flow.Flow<Result> =
        dialogDisplayer.showDialog(dialogModel)

    fun hideAllDialogs() = dialogDisplayer.hideAllDialogs()

    @OptIn(FlowPreview::class)
    protected fun <T> kotlinx.coroutines.flow.Flow<T>.singleTask(taskId: String): kotlinx.coroutines.flow.Flow<T> =
        flow {
            if (singleTasksRegistry.acquire(taskId)) {
                emit(Unit)
            } else {
                throw IllegalConcurrentAccessException()
            }
        }.flatMapConcat {
            this
        }.onCompletion { cause ->
            if (cause !is IllegalConcurrentAccessException) {
                singleTasksRegistry.release(taskId)
            }
        }.catch { e ->
            if (e !is IllegalConcurrentAccessException) {
                throw e
            }
        }

    protected suspend fun <T> singleTask(taskId: String, action: suspend () -> T): T? {
        if (!singleTasksRegistry.acquire(taskId)) {
            return null
        }

        return try {
            action.invoke()
        } finally {
            singleTasksRegistry.release(taskId)
        }
    }
}