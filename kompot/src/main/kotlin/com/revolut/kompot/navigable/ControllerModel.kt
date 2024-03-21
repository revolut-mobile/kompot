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

@file:OptIn(ExperimentalKompotApi::class)

package com.revolut.kompot.navigable

import android.os.SystemClock
import androidx.annotation.CallSuper
import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.ControllerDescriptor
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
import com.revolut.kompot.common.NavigationRequest
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.utils.collectTillFinish
import com.revolut.kompot.navigable.utils.collectTillHide
import com.revolut.kompot.navigable.utils.getController
import com.revolut.kompot.navigable.utils.hideAllDialogs
import com.revolut.kompot.navigable.utils.hideDialog
import com.revolut.kompot.navigable.utils.navigate
import com.revolut.kompot.navigable.utils.setBlockingLoadingVisibility
import com.revolut.kompot.navigable.utils.showDialog
import com.revolut.kompot.navigable.utils.showModal
import com.revolut.kompot.navigable.utils.singleTask
import com.revolut.kompot.navigable.utils.single_task.SingleTasksRegistry
import com.revolut.kompot.navigable.utils.tillFinish
import com.revolut.kompot.navigable.utils.tillHide
import com.revolut.kompot.navigable.utils.withLoading
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.utils.ContextId
import com.revolut.kompot.utils.ContextId.CreatedScopeContextId
import com.revolut.kompot.utils.ContextId.ShownScopeContextId
import com.revolut.kompot.utils.ControllerScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.plus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    private lateinit var extensions: Set<ControllerModelExtension>

    private val singleTasksRegistry = SingleTasksRegistry()

    open fun injectDependencies(
        dialogDisplayer: DialogDisplayer,
        eventsDispatcher: EventsDispatcher,
        controllersCache: ControllersCache,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        controllerModelExtensions: Set<ControllerModelExtension> = emptySet(),
    ) {
        _dialogDisplayer = dialogDisplayer
        _eventsDispatcher = eventsDispatcher
        _controllersCache = controllersCache
        _mainDispatcher = mainDispatcher

        _shownScope += mainDispatcher
        _createdScope += mainDispatcher

        extensions = controllerModelExtensions.onEach { extension -> extension.init(this) }
    }

    open fun setBlockingLoadingVisibility(visible: Boolean, immediate: Boolean = false) {
        setBlockingLoadingVisibility(dialogDisplayer, visible, immediate)
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
        extensions.forEach { it.onParentLifecycleEvent(event) }
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
    ): Job = collectTillHide(
        eventsDispatcher = eventsDispatcher,
        lastLifecycleEvent = lastLifecycleEvent,
        shownScope = shownScope,
        handleError = handleError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach,
    )

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.collectTillFinish(
        handleError: suspend (Throwable) -> Boolean = { false },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job = collectTillFinish(
        eventsDispatcher = eventsDispatcher,
        lastLifecycleEvent = lastLifecycleEvent,
        createdScope = createdScope,
        handleError = handleError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach,
    )

    protected fun <T> tillFinish(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job = tillFinish(
        eventsDispatcher = eventsDispatcher,
        createdScope = createdScope,
        lastLifecycleEvent = lastLifecycleEvent,
        handleError = handleError,
        block = block,
    )

    protected fun <T> tillHide(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job = tillHide(
        eventsDispatcher = eventsDispatcher,
        shownScope = shownScope,
        lastLifecycleEvent = lastLifecycleEvent,
        handleError = handleError,
        block = block,
    )

    suspend fun <T> withLoading(block: suspend () -> T): T = withLoading(
        dialogDisplayer = dialogDisplayer,
        mainDispatcher = mainDispatcher,
        block = block,
    )

    @Suppress("FunctionName")
    private fun ModelScope(contextId: ContextId): CoroutineScope =
        ControllerScope() + mainDispatcher + contextId

    open fun handleErrorEvent(throwable: Throwable): Boolean = false

    open fun tryHandleEvent(event: Event): EventResult? {
        if (event is ErrorEvent && handleErrorEvent(event.throwable)) {
            return ErrorEventResult(true)
        }

        return null
    }

    fun navigate(internalDestination: InternalDestination<*>) = navigate(eventsDispatcher, internalDestination)

    fun NavigationDestination.navigate() = navigate(eventsDispatcher)

    fun <T : IOData.Output> ControllerDescriptor<T>.getController() = getController(eventsDispatcher)

    suspend fun NavigationRequest.navigate() = navigate(eventsDispatcher)

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    fun <T : IOData.Output> Screen<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(eventsDispatcher, style, onResult)

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    fun <T : IOData.Output> Flow<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(eventsDispatcher, style, onResult)

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    suspend fun <T : IOData.Output> Flow<T>.showModalSuspend(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    ): T = suspendCoroutine { emitter ->
        showModal(eventsDispatcher, style, emitter::resume)
    }

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    @OptIn(ExperimentalKompotApi::class)
    fun <T : IOData.Output> ScrollerFlow<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(eventsDispatcher, style, onResult)

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    fun <T : IOData.Output> ViewController<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(eventsDispatcher, style, onResult)

    @Deprecated("This call doesn't support saved state. Use ModalCoordinator or FlowCoordinator to dispatch modals", ReplaceWith(""))
    suspend fun <T : IOData.Output> Screen<T>.showModalSuspend(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    ): T = suspendCoroutine { emitter ->
        showModal(eventsDispatcher, style, emitter::resume)
    }

    fun <Result : DialogModelResult> showDialog(dialogModel: DialogModel<Result>): kotlinx.coroutines.flow.Flow<Result> =
        showDialog(dialogDisplayer, dialogModel)

    fun hideDialog(dialogModel: DialogModel<*>) =
        hideDialog(dialogDisplayer, dialogModel)

    fun hideAllDialogs() = hideAllDialogs(dialogDisplayer)

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.singleTask(taskId: String): kotlinx.coroutines.flow.Flow<T> =
        singleTask(singleTasksRegistry, taskId)

    protected suspend fun <T> singleTask(taskId: String, action: suspend () -> T): T? {
        return singleTask(singleTasksRegistry, taskId, action)
    }
}