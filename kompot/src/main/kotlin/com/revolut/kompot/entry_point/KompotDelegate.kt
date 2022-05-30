package com.revolut.kompot.entry_point

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
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

internal class KompotDelegate(
    private val rootFlow: RootFlow<*, *>,
    @LayoutRes private val defaultFlowLayout: Int?,
    private val trimCacheThreshold: Int,
    private val savedStateEnabled: Boolean = true,
    private val fullScreenEnabled: Boolean = true,
) : LifecycleObserver, HooksProvider {

    private lateinit var controllerManager: RootControllerManager

    private var kompotHost: SavedStateRegistryOwner? = null
    private val hooks = mutableMapOf<ControllerHook.Key<*>, ControllerHook>()

    fun onViewCreated(fragment: Fragment) {
        val rootManagerCreated = ::controllerManager.isInitialized
        if (!rootManagerCreated) {
            setUpWindow(fragment.requireActivity())
            createKompotRoot(
                container = fragment.view as ViewGroup,
                activityLauncher = ActivityFromFragmentLauncher(fragment),
                permissionsRequester = FragmentPermissionsRequester(fragment),
                savedStateOwner = fragment,
            )
        } else {
            //If root controller manager is already created, then onViewCreated
            //is invoked more than one time. That means that fragment's view was recreated
            //and we need to show the root flow in the new container
            controllerManager.attachToHostContainer(container = fragment.view as ViewGroup)
        }

        fragment.viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    controllerManager.detachFromHostContainer()
                    fragment.viewLifecycleOwner.lifecycle.removeObserver(this)
                }
            }
        )
    }

    private fun setUpWindow(hostActivity: ComponentActivity) {
        if (fullScreenEnabled) {
            hostActivity.window.decorView.systemUiVisibility = hostActivity.window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun createKompotRoot(
        container: ViewGroup,
        savedStateOwner: SavedStateRegistryOwner,
        activityLauncher: ActivityLauncher,
        permissionsRequester: PermissionsRequester,
    ) {
        val savedState = savedStateOwner
            .savedStateRegistry
            .takeIf { savedStateEnabled }
            ?.consumeRestoredStateForKey(KOMPOT_SAVED_STATE_KEY)

        controllerManager = RootControllerManager(
            controllersCache = DefaultControllersCache(trimCacheThreshold),
            defaultFlowLayout = defaultFlowLayout,
            activityLauncher = activityLauncher,
            permissionsRequester = permissionsRequester,
            rootFlow = rootFlow,
            hooksProvider = this,
        ).apply {
            showRootFlow(savedState, container)
        }

        savedStateOwner
            .savedStateRegistry
            .takeIf { savedStateEnabled }
            ?.registerSavedStateProvider(KOMPOT_SAVED_STATE_KEY) {
                Bundle().apply {
                    controllerManager.saveState(this)
                }
            }

        savedStateOwner.lifecycle.addObserver(this)
        kompotHost = savedStateOwner
    }

    fun registerHook(hook: ControllerHook, key: ControllerHook.Key<*>) {
        hooks[key] = hook
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        controllerManager.onHostResumed()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        controllerManager.onHostPaused()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        controllerManager.onHostStarted()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        controllerManager.onHostStopped()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        controllerManager.onDestroy()
        kompotHost?.lifecycle?.removeObserver(this)
        kompotHost?.savedStateRegistry?.unregisterSavedStateProvider(KOMPOT_SAVED_STATE_KEY)
        kompotHost = null
    }

    fun onBackPressed() {
        controllerManager.handleBack()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        controllerManager.onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.isNotEmpty()) {
            controllerManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ControllerHook> getHook(key: ControllerHook.Key<T>): T? = hooks[key] as? T
}

private const val KOMPOT_SAVED_STATE_KEY = "KOMPOT_SAVED_STATE_KEY"