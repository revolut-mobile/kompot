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

package com.revolut.kompot.navigable.vc.binding

import android.content.Intent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.SavedStateOwner
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.vc.ui.PersistentModelStateStorage

interface ViewControllerModelApi<Output : IOData.Output> {
    fun resultsBinder(): ModelBinder<Output>
    fun backPressBinder(): ModelBinder<Unit>
}

interface ModelBinding : SavedStateOwner {
    fun onCreate() = Unit
    fun onDestroy() = Unit
    fun onShow() = Unit
    fun onHide() = Unit
    fun onTransitionStart(enter: Boolean) = Unit
    fun onTransitionEnd(enter: Boolean) = Unit
    fun onHostPaused() = Unit
    fun onHostResumed() = Unit
    fun onHostStarted() = Unit
    fun onHostStopped() = Unit
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit
    fun handleBack(defaultHandler: () -> Boolean) = defaultHandler()
    fun handleQuit(): Boolean = false
    fun onParentManagerCleared() = Unit

    fun restoreStateFromStorage(stateStorage: PersistentModelStateStorage) = Unit
    fun saveStateToStorage(stateStorage: PersistentModelStateStorage) = Unit
}