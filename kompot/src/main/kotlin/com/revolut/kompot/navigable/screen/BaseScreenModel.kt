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

package com.revolut.kompot.navigable.screen

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.screen.state.SaveStateDelegate
import com.revolut.kompot.utils.MutableBehaviourFlow
import com.revolut.kompot.navigable.binder.ModelBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex

abstract class BaseScreenModel<STATE : ScreenStates.Domain, UI : ScreenStates.UI, OUTPUT : IOData.Output>(private val stateMapper: StateMapper<STATE, UI>) : ControllerModel(),
    ScreenModel<UI, OUTPUT> {

    protected abstract val initialState: STATE

    protected open val mapStateInBackground = false

    private val resultCommandsBinder = ModelBinder<OUTPUT>()
    private val backCommandsBinder = ModelBinder<Unit>()

    private val stateFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableBehaviourFlow<STATE>().apply {
            tryEmit(initialState)
        }
    }
    protected val state: STATE
        get() = checkNotNull(stateFlow.replayCache.firstOrNull()) {
            "initialState must be initialised to use coroutines API"
        }

    protected open val saveStateDelegate: SaveStateDelegate<STATE, *>? = null

    final override fun resultsBinder(): ModelBinder<OUTPUT> = resultCommandsBinder

    @OptIn(FlowPreview::class)
    final override fun uiStateStream(): Flow<UI> =
        domainStateStream()
            .withIndex()
            .flatMapConcat {
                flowOf(it.value)
                    .map(stateMapper::mapState)
                    .run {
                        if (it.index > 0 && mapStateInBackground) {
                            flowOn(Dispatchers.Default)
                        } else {
                            this
                        }
                    }
            }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    open fun domainStateStream(): Flow<STATE> = stateFlow.distinctUntilChanged()

    protected fun updateState(func: STATE.() -> STATE) {
        stateFlow.tryEmit(state.func())
    }

    fun postScreenResult(result: OUTPUT) {
        resultCommandsBinder.notify(result)
    }

    final override fun backPressBinder(): ModelBinder<Unit> = backCommandsBinder

    fun postBack() {
        backCommandsBinder.notify(Unit)
    }

    override fun saveState(): Bundle = Bundle().apply {
        saveStateDelegate?.getRetainedState(state)?.let { retainedState ->
            putParcelable(DOMAIN_STATE_KEY, retainedState)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.classLoader = javaClass.classLoader
        state.getParcelable<Parcelable>(DOMAIN_STATE_KEY)?.let { retainedState ->
            val restoredDomainState = requireNotNull(saveStateDelegate)
                .restoreDomainStateInternal(initialState, retainedState)

            stateFlow.tryEmit(restoredDomainState)
        }
    }

    final override fun onLifecycleEvent(event: LifecycleEvent) {
        super.onLifecycleEvent(event)
    }

    companion object {

        private const val DOMAIN_STATE_KEY = "DOMAIN_STATE_KEY"
    }

}
