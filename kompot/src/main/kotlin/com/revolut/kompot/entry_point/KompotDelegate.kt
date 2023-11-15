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

package com.revolut.kompot.entry_point

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.savedstate.SavedStateRegistryOwner
import com.revolut.kompot.common.ActivityFromFragmentLauncher
import com.revolut.kompot.common.ActivityLauncher
import com.revolut.kompot.common.FragmentPermissionsRequester
import com.revolut.kompot.common.PermissionsRequester
import com.revolut.kompot.navigable.RootControllerManager
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.hooks.ControllerHook
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.utils.KompotIllegalLifecycleException

internal class KompotDelegate(
    private val rootFlow: RootFlow<*, *>,
    @LayoutRes private val defaultFlowLayout: Int?,
    private val trimCacheThreshold: Int,
    private val savedStateEnabled: Boolean = true,
    private val fullScreenEnabled: Boolean = true,
) : LifecycleObserver, HooksProvider {

    private var rootControllerManager: RootControllerManager? = null
    private var kompotHost: Fragment? = null

    private val hooks = mutableMapOf<ControllerHook.Key<*>, ControllerHook>()

    fun onViewCreated(fragment: Fragment) {
        val rootInitialised = rootControllerManager != null
        if (!rootInitialised) {
            val rootFlowWasCreated = rootFlow.created
            if (rootFlowWasCreated) {
                (rootFlow.view.parent as? ViewGroup)?.removeView(rootFlow.view)
            }

            setUpWindow(fragment.requireActivity())
            kompotHost = fragment
            rootControllerManager = createKompotRoot(
                container = fragment.view as ViewGroup,
                activityLauncher = ActivityFromFragmentLauncher(fragment),
                permissionsRequester = FragmentPermissionsRequester(fragment),
                savedStateOwner = fragment,
            )

            fragment
                .savedStateRegistry
                .takeIf { savedStateEnabled }
                ?.registerSavedStateProvider(KOMPOT_SAVED_STATE_KEY) {
                    Bundle().apply {
                        rootControllerManager?.saveState(this)
                    }
                }

            fragment.lifecycle.addObserver(this)

            if (rootFlowWasCreated) {
                throw KompotIllegalLifecycleException(
                    "Can't initialise Kompot with the pre-created flow: ${rootFlow.controllerName} is ${rootFlow.lifecycle.currentState}"
                )
            }
        } else {
            //If root controller manager is already created, then onViewCreated
            //is invoked more than one time. That means that fragment's view was recreated
            //and we need to show the root flow in the new container
            requireRootControllerManager().apply {
                detachFromHostContainer()
                attachToHostContainer(container = fragment.view as ViewGroup)
            }
        }

        fragment.viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    requireRootControllerManager().detachFromHostContainer()
                    fragment.viewLifecycleOwner.lifecycle.removeObserver(this)
                }
            }
        )
    }

    private fun setUpWindow(hostActivity: ComponentActivity) {
        if (fullScreenEnabled) {
            WindowCompat.setDecorFitsSystemWindows(hostActivity.window, false)
        }
    }

    private fun createKompotRoot(
        container: ViewGroup,
        savedStateOwner: SavedStateRegistryOwner,
        activityLauncher: ActivityLauncher,
        permissionsRequester: PermissionsRequester,
    ): RootControllerManager {
        val savedState = savedStateOwner
            .savedStateRegistry
            .takeIf { savedStateEnabled }
            ?.consumeRestoredStateForKey(KOMPOT_SAVED_STATE_KEY)

        return RootControllerManager(
            controllersCache = DefaultControllersCache(trimCacheThreshold),
            defaultFlowLayout = defaultFlowLayout,
            activityLauncher = activityLauncher,
            permissionsRequester = permissionsRequester,
            rootFlow = rootFlow,
            hooksProvider = this,
        ).apply {
            showRootFlow(savedState, container)
        }
    }

    fun registerHook(hook: ControllerHook, key: ControllerHook.Key<*>) {
        hooks[key] = hook
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        requireRootControllerManager().onHostResumed()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        requireRootControllerManager().onHostPaused()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        requireRootControllerManager().onHostStarted()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        requireRootControllerManager().onHostStopped()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        requireRootControllerManager().onDestroy()
        kompotHost?.lifecycle?.removeObserver(this)
        kompotHost?.savedStateRegistry?.unregisterSavedStateProvider(KOMPOT_SAVED_STATE_KEY)
        kompotHost = null
    }

    fun onBackPressed() {
        requireRootControllerManager().handleBack()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        requireRootControllerManager().onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.isNotEmpty()) {
            requireRootControllerManager().onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun requireRootControllerManager() = checkNotNull(rootControllerManager)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ControllerHook> getHook(key: ControllerHook.Key<T>): T? = hooks[key] as? T
}

private const val KOMPOT_SAVED_STATE_KEY = "KOMPOT_SAVED_STATE_KEY"