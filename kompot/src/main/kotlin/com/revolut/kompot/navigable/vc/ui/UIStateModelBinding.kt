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

package com.revolut.kompot.navigable.vc.ui

import android.os.Bundle
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.common.PersistableState
import com.revolut.kompot.navigable.vc.common.PersistableStorageState
import com.revolut.kompot.utils.DEFAULT_EXTRA_BUFFER_CAPACITY
import com.revolut.kompot.utils.debounceButEmitFirst
import com.revolut.kompot.utils.withPrevious
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take

typealias DebounceStreamProvider = () -> Flow<Any>

interface UIStatesModelBinding<UI : States.UI> : ModelBinding

internal class UIStateModelBindingImpl<M : UIStatesModel<D, UI, Out>, D : States.Domain, UI : States.UI, Out : IOData.Output>(
    private val controller: UIStatesController<UI>,
    val model: M,
    private val debounceStreamProvider: DebounceStreamProvider?,
) : UIStatesModelBinding<UI> {

    private val viewController: ViewController<*> get() = controller as ViewController<*>
    private val bindScreenFlow = MutableSharedFlow<UI>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val transitionEndFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = DEFAULT_EXTRA_BUFFER_CAPACITY)
    var doBeforeRender: ((UI) -> Unit)? = null

    override fun onCreate() {
        model.state.onLifecycleEvent(LifecycleEvent.CREATED)
        startCollectingUIState()
    }

    override fun onShow() {
        model.state.onLifecycleEvent(LifecycleEvent.SHOWN)
        bindUIState()
    }

    override fun onHide() {
        model.state.onLifecycleEvent(LifecycleEvent.HIDDEN)
    }

    override fun onDestroy() {
        model.state.onLifecycleEvent(LifecycleEvent.FINISHED)
    }

    private fun startCollectingUIState() {
        val debouncedUIStateStream = debounceStreamProvider?.invoke()?.let { flow ->
            combine(
                flow.onStart { emit(Unit) },
                model.state.uiStates()
            ) { _, uiState ->
                uiState
            }.debounceButEmitFirst(300)
        }

        (debouncedUIStateStream ?: model.state.uiStates())
            .onEach(bindScreenFlow::emit)
            .launchIn(viewController.createdScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bindUIState() {
        bindScreenFlow
            .flatMapLatest { state ->
                if (viewController.activeTransition == Controller.ActiveTransition.NONE) {
                    flowOf(state)
                } else {
                    transitionEndFlow
                        .filter { enter -> enter }
                        .take(1)
                        .map { state }
                }
            }
            .withPrevious()
            .onEach { (prevState, state) ->
                doBeforeRender?.invoke(state)
                controller.render(state, prevState?.let(state::calculatePayload))
            }
            .launchIn(viewController.attachedScope)
    }

    override fun onTransitionEnd(enter: Boolean) {
        transitionEndFlow.tryEmit(enter)
    }

    override fun saveState(outState: Bundle) {
        (model.state as? PersistableState)?.saveState(outState)
    }

    override fun restoreState(state: Bundle) {
        (model.state as? PersistableState)?.restoreState(state)
    }

    override fun restoreStateFromStorage(stateStorage: PersistentModelStateStorage) {
        (model.state as? PersistableStorageState)?.restoreStateFromStorage(stateStorage)
    }

    override fun saveStateToStorage(stateStorage: PersistentModelStateStorage) {
        (model.state as? PersistableStorageState)?.saveStateToStorage(stateStorage)
    }
}

@Suppress("FunctionName")
fun <M : UIStatesModel<D, UI, Out>, D : States.Domain, UI : States.UI, Out : IOData.Output> UIStatesController<UI>.ModelBinding(
    model: M,
    debounceStreamProvider: DebounceStreamProvider? = null,
): UIStatesModelBinding<UI> {
    return UIStateModelBindingImpl(
        controller = this,
        model = model,
        debounceStreamProvider = debounceStreamProvider,
    )
}