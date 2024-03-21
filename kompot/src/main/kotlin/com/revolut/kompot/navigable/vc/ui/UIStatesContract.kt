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

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi
import com.revolut.kompot.navigable.vc.common.StateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean

interface UIStatesController<UI : States.UI> : ViewControllerApi {
    fun render(uiState: UI, payload: Any?)
    override val modelBinding: UIStatesModelBinding<UI>
}

interface UIStatesModel<Domain : States.Domain, UI : States.UI, Out : IOData.Output> :
    ViewControllerModelApi<Out> {
    val state: ModelState<Domain, UI>
}

abstract class ModelState<Domain : States.Domain, UI : States.UI>(
    initialState: Domain,
    private val stateMapper: States.Mapper<Domain, UI>,
    private val mapStateInBackground: Boolean,
) : StateHolder<Domain>(initialState) {

    private val firstEmmitHappened = AtomicBoolean(false)

    @OptIn(FlowPreview::class)
    fun uiStates(): Flow<UI> =
        if (mapStateInBackground) {
            statesStream()
                .flatMapConcat {
                    flowOf(it)
                        .map(stateMapper::mapState)
                        .run {
                            if (firstEmmitHappened.getAndSet(true)) {
                                flowOn(Dispatchers.Default)
                            } else {
                                this
                            }
                        }
                }
        } else {
            statesStream()
                .map(stateMapper::mapState)
        }

    internal fun domainStateStream(): Flow<Domain> = statesStream()

    internal fun onLifecycleEvent(event: LifecycleEvent) {
        if (event == LifecycleEvent.SHOWN) {
            firstEmmitHappened.set(false)
        }
    }
}