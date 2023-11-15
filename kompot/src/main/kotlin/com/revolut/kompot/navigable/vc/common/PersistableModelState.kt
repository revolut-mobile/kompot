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

internal class PersistableStateHolderImpl<S : Parcelable>(
    initialState: S,
) : PersistableStateHolder<S>(initialState) {

    override fun saveState(bundle: Bundle) {
        bundle.putParcelable(SAVED_STATE_KEY, current)
    }

    override fun restoreState(bundle: Bundle) {
        bundle.classLoader = javaClass.classLoader
        bundle.getParcelable<S>(SAVED_STATE_KEY)?.let { restoredState ->
            update { restoredState }
        }
    }

    private companion object {
        private const val SAVED_STATE_KEY = "PersistableStateKey"
    }
}

@Suppress("FunctionName")
fun <S : Parcelable, Output : IOData.Output> PersistableStateModel<S, Output>.ModelState(
    initialState: S,
): PersistableStateHolder<S> = PersistableStateHolderImpl(
    initialState = initialState,
)