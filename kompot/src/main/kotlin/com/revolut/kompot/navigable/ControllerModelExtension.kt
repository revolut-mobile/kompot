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

import androidx.annotation.VisibleForTesting
import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.ControllerDescriptor
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
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.utils.collectTillFinish
import com.revolut.kompot.navigable.utils.collectTillHide
import com.revolut.kompot.navigable.utils.getController
import com.revolut.kompot.navigable.utils.hideAllDialogs
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class ControllerModelExtension {

    private lateinit var parent: ControllerModel
    private val parentMainDispatcher: CoroutineDispatcher
        get() = parent.mainDispatcher
    private val parentCreatedScope: CoroutineScope
        get() = parent.createdScope
    private val parentShownScope: CoroutineScope
        get() = parent.shownScope
    private var parentLastLifecycleEvent: LifecycleEvent? = null

    val parentDialogDisplayer: DialogDisplayer
        get() = parent.dialogDisplayer

    val parentEventsDispatcher: EventsDispatcher
        get() = parent.eventsDispatcher

    private val singleTasksRegistry = SingleTasksRegistry()

    @VisibleForTesting
    fun init(parent: ControllerModel) {
        this.parent = parent
    }

    internal fun onParentLifecycleEvent(event: LifecycleEvent) {
        parentLastLifecycleEvent = event
        when (event) {
            LifecycleEvent.CREATED -> {
                onCreated()
            }

            LifecycleEvent.SHOWN -> {
                onShown()
            }

            LifecycleEvent.HIDDEN -> {
                onHidden()
            }

            LifecycleEvent.FINISHED -> {
                onFinished()
            }
        }
    }

    open fun onCreated() = Unit

    open fun onShown() = Unit

    open fun onHidden() = Unit

    open fun onFinished() = Unit

    protected fun setBlockingLoadingVisibility(visible: Boolean, immediate: Boolean = false) {
        setBlockingLoadingVisibility(parentDialogDisplayer, visible, immediate)
    }

    protected suspend fun <T> withLoading(block: suspend () -> T): T = withLoading(
        dialogDisplayer = parentDialogDisplayer,
        mainDispatcher = parentMainDispatcher,
        block = block,
    )

    protected fun <T> tillHide(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job = tillHide(
        eventsDispatcher = parentEventsDispatcher,
        shownScope = parentShownScope,
        lastLifecycleEvent = parentLastLifecycleEvent,
        handleError = handleError,
        block = block,
    )

    protected fun <T> tillFinish(
        handleError: (Throwable) -> Boolean = { false },
        block: suspend CoroutineScope.() -> T
    ): Job = tillFinish(
        eventsDispatcher = parentEventsDispatcher,
        createdScope = parentCreatedScope,
        lastLifecycleEvent = parentLastLifecycleEvent,
        handleError = handleError,
        block = block,
    )

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.collectTillHide(
        handleError: suspend (Throwable) -> Boolean = { false },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job = collectTillHide(
        eventsDispatcher = parentEventsDispatcher,
        lastLifecycleEvent = parentLastLifecycleEvent,
        shownScope = parentShownScope,
        handleError = handleError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach,
    )

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.collectTillFinish(
        handleError: suspend (Throwable) -> Boolean = { false },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job = collectTillFinish(
        eventsDispatcher = parentEventsDispatcher,
        lastLifecycleEvent = parentLastLifecycleEvent,
        createdScope = parentCreatedScope,
        handleError = handleError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach,
    )

    protected fun navigate(internalDestination: InternalDestination<*>) = navigate(parentEventsDispatcher, internalDestination)

    protected fun NavigationDestination.navigate() = navigate(parentEventsDispatcher)

    fun <T : IOData.Output> ControllerDescriptor<T>.getController() = getController(parentEventsDispatcher)

    suspend fun NavigationRequest.navigate() = navigate(parentEventsDispatcher)

    protected fun <T : IOData.Output> Screen<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(parentEventsDispatcher, style, onResult)

    protected fun <T : IOData.Output> Flow<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(parentEventsDispatcher, style, onResult)

    @OptIn(ExperimentalKompotApi::class)
    protected fun <T : IOData.Output> ScrollerFlow<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(parentEventsDispatcher, style, onResult)

    protected fun <T : IOData.Output> ViewController<T>.showModal(
        style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
        onResult: ((T) -> Unit)? = null
    ) = showModal(parentEventsDispatcher, style, onResult)

    protected fun <Result : DialogModelResult> showDialog(dialogModel: DialogModel<Result>): kotlinx.coroutines.flow.Flow<Result> =
        showDialog(parentDialogDisplayer, dialogModel)

    protected fun hideAllDialogs() = hideAllDialogs(parentDialogDisplayer)

    protected fun <T> kotlinx.coroutines.flow.Flow<T>.singleTask(taskId: String): kotlinx.coroutines.flow.Flow<T> =
        singleTask(singleTasksRegistry, taskId)

    protected suspend fun <T> singleTask(taskId: String, action: suspend () -> T): T? {
        return singleTask(singleTasksRegistry, taskId, action)
    }

}