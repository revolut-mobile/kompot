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

package com.revolut.kompot.navigable.extension

import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.utils.MutableBehaviourFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

abstract class BaseStatefulControllerModelExtension<STATE> : ControllerModelExtension(), StatefulControllerModelExtension<STATE> {

    protected abstract val initialState: STATE

    private val stateFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableBehaviourFlow<STATE>().apply { tryEmit(initialState) }
    }
    protected val state: STATE
        get() = checkNotNull(stateFlow.replayCache.firstOrNull()) { "initialState must be initialised to use coroutines API" }

    final override fun stateStream(): Flow<STATE> = stateFlow.distinctUntilChanged()

    protected fun updateState(func: STATE.() -> STATE) {
        stateFlow.tryEmit(state.func())
    }

}