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

package com.revolut.kompot.navigable.vc.composite

import android.content.Intent
import android.os.Bundle
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.ui.PersistentModelStateStorage

internal class CompositeModelBinding(
    private val bindings: List<ModelBinding>
) : ModelBinding {

    override fun onCreate() {
        bindings.forEach { binding ->
            binding.onCreate()
        }
    }

    override fun onDestroy() {
        bindings.forEach { binding ->
            binding.onDestroy()
        }
    }

    override fun onShow() {
        bindings.forEach { binding ->
            binding.onShow()
        }
    }

    override fun onHide() {
        bindings.forEach { binding ->
            binding.onHide()
        }
    }

    override fun onTransitionStart(enter: Boolean) {
        bindings.forEach { binding ->
            binding.onTransitionStart(enter)
        }
    }

    override fun onTransitionEnd(enter: Boolean) {
        bindings.forEach { binding ->
            binding.onTransitionEnd(enter)
        }
    }

    override fun onHostPaused() {
        bindings.forEach { binding ->
            binding.onHostPaused()
        }
    }

    override fun onHostResumed() {
        bindings.forEach { binding ->
            binding.onHostResumed()
        }
    }

    override fun onHostStarted() {
        bindings.forEach { binding ->
            binding.onHostStarted()
        }
    }

    override fun onHostStopped() {
        bindings.forEach { binding ->
            binding.onHostStopped()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        bindings.forEach { binding ->
            binding.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        bindings.forEach { binding ->
            binding.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun handleBack(defaultHandler: () -> Boolean) =
        bindings.any { it.handleBack(defaultHandler) }

    override fun saveState(outState: Bundle) {
        bindings.forEach { binding ->
            binding.saveState(outState)
        }
    }

    override fun restoreState(state: Bundle) {
        bindings.forEach { binding ->
            binding.restoreState(state)
        }
    }

    override fun saveStateToStorage(stateStorage: PersistentModelStateStorage) {
        bindings.forEach { binding ->
            binding.saveStateToStorage(stateStorage)
        }
    }

    override fun restoreStateFromStorage(stateStorage: PersistentModelStateStorage) {
        bindings.forEach { binding ->
            binding.restoreStateFromStorage(stateStorage)
        }
    }
}