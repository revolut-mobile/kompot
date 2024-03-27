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

package com.revolut.kompot.navigable

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.revolut.kompot.common.ActivityLauncher
import com.revolut.kompot.common.PermissionsRequester
import com.revolut.kompot.holder.RootControllerViewHolder
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.root.RootFlow

internal class RootControllerManager(
    private val rootFlow: RootFlow<*, *>,
    private val activityLauncher: ActivityLauncher,
    private val permissionsRequester: PermissionsRequester,
    @LayoutRes defaultControllerContainer: Int?,
    controllersCache: ControllersCache,
    hooksProvider: HooksProvider,
) : ControllerManager(
    modal = false,
    defaultControllerContainer = defaultControllerContainer,
    controllersCache = controllersCache,
    controllerViewHolder = RootControllerViewHolder(),
    onAttachController = null,
    onDetachController = null,
    onTransitionCanceled = null,
), ActivityLauncher by activityLauncher, PermissionsRequester by permissionsRequester {

    private val rootControllerViewHolder
        get() = controllerViewHolder as RootControllerViewHolder

    init {
        this.hooksProvider = hooksProvider
    }

    fun showRootFlow(savedState: Bundle?, hostContainer: ViewGroup) {
        rootControllerViewHolder.setContainer(hostContainer)

        savedState?.let { bundle ->
            rootFlow.doOnCreate {
                rootFlow.restoreState(bundle)
            }
        }
        showImmediately(rootFlow)
    }

    fun attachToHostContainer(container: ViewGroup) {
        rootControllerViewHolder.setContainer(container)
        activeController?.view?.let(controllerViewHolder::add)
    }

    fun detachFromHostContainer() {
        activeController?.view?.let(controllerViewHolder::remove)
        rootControllerViewHolder.removeContainer()
    }

    internal fun saveState(outState: Bundle) {
        rootFlow.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        controllersCache.clearCache()
    }

}