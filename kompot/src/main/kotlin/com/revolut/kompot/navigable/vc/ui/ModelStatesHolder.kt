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
import android.os.Parcelable
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.common.PersistableState
import com.revolut.kompot.navigable.vc.common.PersistableStorageState
import kotlinx.coroutines.CoroutineDispatcher

internal class UIStatesImpl<Domain : States.Domain, UI : States.UI>(
    private val initialState: Domain,
    stateMapper: States.Mapper<Domain, UI>,
    mapStatesInBackground: Boolean,
    private val saveStateDelegate: SaveStateDelegate<Domain, *>?,
) : ModelState<Domain, UI>(initialState, stateMapper, mapStatesInBackground), PersistableState {

    override fun saveState(bundle: Bundle) {
        saveStateDelegate?.getRetainedState(current)?.let { retainedState ->
            bundle.putParcelable(DOMAIN_STATE_SNAPSHOT_KEY, retainedState)
        }
    }

    override fun restoreState(bundle: Bundle) {
        bundle.classLoader = javaClass.classLoader
        bundle.getParcelable<Parcelable>(DOMAIN_STATE_SNAPSHOT_KEY)?.let { retainedState ->
            val restoredDomainState = requireNotNull(saveStateDelegate)
                .restoreDomainStateInternal(initialState, retainedState)

            update { restoredDomainState }
        }
    }

    private companion object {
        const val DOMAIN_STATE_SNAPSHOT_KEY = "DomainStateSnapshotKey"
    }
}

internal class PersistentUIStatesImpl<Domain : States.PersistentDomain, UI : States.UI>(
    private val key: PersistentModelStateKey,
    private val restoredStateReducer: (Domain, Domain) -> Domain,
    private val initialState: Domain,
    stateMapper: States.Mapper<Domain, UI>,
    mapStatesInBackground: Boolean,
) : ModelState<Domain, UI>(initialState, stateMapper, mapStatesInBackground), PersistableStorageState {

    override fun restoreStateFromStorage(stateStorage: PersistentModelStateStorage) {
        stateStorage.get<Domain>(key)?.let { restoredState ->
            update {
                restoredStateReducer(initialState, restoredState)
            }
        }
    }

    override fun saveStateToStorage(stateStorage: PersistentModelStateStorage) {
        stateStorage.put(key, current)
    }
}

abstract class SaveStateDelegate<T, R : Parcelable> {

    abstract fun getRetainedState(currentState: T): R?

    abstract fun restoreDomainState(initialState: T, retainedState: R): T

    @Suppress("UNCHECKED_CAST")
    internal fun restoreDomainStateInternal(initialState: T, retainedState: Any): T =
        restoreDomainState(initialState, retainedState as R)

}

@Suppress("FunctionName")
fun <Domain : States.Domain, UI : States.UI, Output : IOData.Output> UIStatesModel<Domain, UI, Output>.ModelState(
    initialState: Domain,
    stateMapper: States.Mapper<Domain, UI>,
    saveStateDelegate: SaveStateDelegate<Domain, *>? = null,
    mapStatesInBackground: Boolean = false,
): ModelState<Domain, UI> = UIStatesImpl(
    stateMapper = stateMapper,
    initialState = initialState,
    mapStatesInBackground = mapStatesInBackground,
    saveStateDelegate = saveStateDelegate,
)

@Suppress("FunctionName")
fun <Domain : States.PersistentDomain, UI : States.UI, Output : IOData.Output> UIStatesModel<Domain, UI, Output>.PersistentModelState(
    key: PersistentModelStateKey,
    initialState: Domain,
    stateMapper: States.Mapper<Domain, UI>,
    mapStatesInBackground: Boolean = false,
    restoredStateReducer: (Domain, Domain) -> Domain = { _, restored -> restored },
): ModelState<Domain, UI> = PersistentUIStatesImpl(
    key = key,
    stateMapper = stateMapper,
    initialState = initialState,
    mapStatesInBackground = mapStatesInBackground,
    restoredStateReducer = restoredStateReducer,
)

@JvmInline
value class PersistentModelStateKey(val keyValue: String)

interface PersistentModelStateStorage {
    fun <T : States.PersistentDomain> get(key: PersistentModelStateKey): T?
    fun remove(key: PersistentModelStateKey)
    fun put(key: PersistentModelStateKey, state: States.PersistentDomain)
    suspend fun prefetchAll()
}