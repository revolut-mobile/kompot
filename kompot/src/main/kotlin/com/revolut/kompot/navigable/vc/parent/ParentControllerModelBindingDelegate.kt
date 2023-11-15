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

package com.revolut.kompot.navigable.vc.parent

import android.content.Intent
import android.os.Bundle
import com.revolut.kompot.navigable.flow.ControllerManagersProvider
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ModelBinding

internal class ParentControllerModelBindingDelegate(
    private val childControllerManagersProvider: ControllerManagersProvider,
    private val controller: ViewControllerApi,
) : ModelBinding {

    private val viewController: ViewController<*> get() = controller as ViewController<*>

    override fun onDestroy() {
        childControllerManagersProvider.all.forEach { manager -> manager.onDestroy() }
    }

    override fun handleBack(defaultHandler: () -> Boolean): Boolean {
        for (manager in childControllerManagersProvider.all.asReversed()) {
            if (manager.handleBack()) {
                return true
            }
        }

        if (defaultHandler()) {
            return true
        }

        return false
    }

    override fun onShow() {
        childControllerManagersProvider.all.asReversed().any { it.onAttach() }
    }

    override fun onHide() {
        childControllerManagersProvider.all.asReversed().forEach { manager -> manager.onDetach() }
    }

    override fun onTransitionStart(enter: Boolean) {
        childControllerManagersProvider.all.forEach { manager ->
            manager.activeController?.onTransitionStart(enter)
        }
    }

    override fun onTransitionEnd(enter: Boolean) {
        childControllerManagersProvider.all.forEach { manager ->
            manager.activeController?.onTransitionEnd(enter)
        }
    }

    override fun onHostPaused() {
        childControllerManagersProvider.all.forEach { manager -> manager.onHostPaused() }
    }

    override fun onHostResumed() {
        childControllerManagersProvider.all.forEach { manager -> manager.onHostResumed() }
    }

    override fun onHostStarted() {
        childControllerManagersProvider.all.forEach { manager -> manager.onHostStarted() }
    }

    override fun onHostStopped() {
        childControllerManagersProvider.all.forEach { manager -> manager.onHostStopped() }
    }

    override fun onParentManagerCleared() {
        childControllerManagersProvider.all.asReversed().forEach {
            it.activeController?.onParentManagerCleared()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewController.doOnAttach {
            childControllerManagersProvider.all.forEach { manager ->
                manager.onActivityResult(
                    requestCode,
                    resultCode,
                    data
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        childControllerManagersProvider.all.forEach { manager ->
            manager.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun saveState(outState: Bundle) = Unit
    override fun restoreState(state: Bundle) = Unit
}