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

internal class PersistableStateBindingImpl<M : PersistableStateModel<State, Out>, State : Parcelable, Out : IOData.Output>(
    private val controller: PersistableStateController,
    private val model: M,
) : PersistableStateModelBinding {
    override fun saveState(outState: Bundle) = model.state.saveState(outState)
    override fun restoreState(state: Bundle) = model.state.restoreState(state)
}