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

package com.revolut.kompot.navigable.vc.common

import android.os.Bundle
import android.os.Parcelable
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi
import com.revolut.kompot.navigable.vc.ui.PersistentModelStateStorage

interface PersistableStateModelBinding : ModelBinding

interface PersistableStateController : ViewControllerApi {
    override val modelBinding: PersistableStateModelBinding
}

interface PersistableStateModel<S : Parcelable, Out : IOData.Output> : ViewControllerModelApi<Out> {
    val state: PersistableStateHolder<S>
}

abstract class PersistableStateHolder<S : Parcelable>(
    initialState: S
) : StateHolder<S>(initialState), PersistableState

interface PersistableStorageState {
    fun restoreStateFromStorage(stateStorage: PersistentModelStateStorage)
    fun saveStateToStorage(stateStorage: PersistentModelStateStorage)
}

interface PersistableState {
    fun saveState(bundle: Bundle)
    fun restoreState(bundle: Bundle)
}