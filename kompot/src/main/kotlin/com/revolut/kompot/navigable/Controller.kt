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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.revolut.kompot.KompotPlugin
import com.revolut.kompot.common.ActivityFromControllerLauncher
import com.revolut.kompot.common.ActivityLauncher
import com.revolut.kompot.common.PermissionsFromControllerRequester
import com.revolut.kompot.common.PermissionsRequester
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.navigable.binder.CompositeBinding
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.hooks.ControllerViewContextHook
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.transition.TransitionCallbacks
import com.revolut.kompot.navigable.utils.ControllerEnvironment
import com.revolut.kompot.navigable.utils.collectTillDetachView
import com.revolut.kompot.utils.ControllerScope
import com.revolut.kompot.view.ControllerContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Suppress("SyntheticAccessor")
abstract class Controller :
    TransitionCallbacks,
    LifecycleOwner,
    ActivityLauncher,
    PermissionsRequester,
    LayoutOwner {

    @StyleRes
    open val themeId: Int? = null
    open val fitStatusBar: Boolean? = null
    open val fitNavigationBar: Boolean? = null
    open var keyInitialization: () -> ControllerKey = { ControllerKey.random() }

    val activity: Activity by lazy(LazyThreadSafetyMode.NONE) {
        var context: Context = view.context
        while (context is ContextWrapper) {
            if (context is Activity) {
                break
            }
            context = context.baseContext
        }

        (context as? Activity) ?: throw IllegalStateException("Activity is not present")
    }
    val key: ControllerKey by lazy(LazyThreadSafetyMode.NONE) {
        keyInitialization()
    }
    val environment: ControllerEnvironment by lazy {
        ControllerEnvironment(this)
    }
    val resources: Resources get() = activity.resources

    internal val createdScope: CoroutineScope = ControllerScope()
    internal val attachedScope: CoroutineScope = ControllerScope()
    internal val tillDestroyBinding = CompositeBinding()

    internal lateinit var parentControllerManager: ControllerManager
    internal val controllersCache: ControllersCache
        get() = parentControllerManager.controllersCache
    internal val hooksProvider: HooksProvider?
        get() = parentControllerManager.hooksProvider

    private val activityLauncher by lazy(LazyThreadSafetyMode.NONE) {
        ActivityFromControllerLauncher(this)
    }
    private val permissionsRequester by lazy(LazyThreadSafetyMode.NONE) {
        PermissionsFromControllerRequester(this)
    }

    protected val parentFlow: ParentFlow
        get() = parentController as ParentFlow

    internal var parentController: Controller? = null
    lateinit var view: View

    private var _attached = false
    val attached: Boolean
        get() = _attached

    private var _detached = false
    val detached: Boolean
        get() = _detached

    private var _created = false
    internal val created: Boolean
        get() = _created

    private var _started = false
    internal val started: Boolean
        get() = _started

    private var _destroyed = false
    internal val destroyed: Boolean
        get() = _destroyed

    private var _activeTransition = ActiveTransition.NONE
    internal val activeTransition: ActiveTransition
        get() = _activeTransition

    private val onCreateCallbacks = mutableListOf<() -> Unit>()
    private val onDestroyCallbacks = mutableListOf<() -> Unit>()
    private val onAttachCallbacks = mutableListOf<() -> Unit>()
    private val onExitTransitionEndCallbacks = mutableListOf<() -> Unit>()
    private val lifecycleRegistry by lazy(LazyThreadSafetyMode.NONE) {
        LifecycleRegistry.createUnsafe(this)
    }
    var backEnabled = true

    open val controllerName: String
        get() = javaClass.simpleName

    val fullControllerName: String
        get() {
            val parentName = parentController?.fullControllerName?.let { name -> "${name.removeSuffix("Flow")}." } ?: ""
            return "$parentName${controllerName.removeSuffix("Screen")}"
        }

    open var cacheStrategy: ControllerCacheStrategy = ControllerCacheStrategy.Auto

    open val controllerExtensions: Set<ControllerExtension> = emptySet()

    open fun createView(inflater: LayoutInflater): View =
        inflater.inflate(layoutId, null, false).also {
            this.view = it
        }

    internal fun getViewInflater(baseInflater: LayoutInflater): LayoutInflater {
        val themeId = themeId
        val controllerViewCtxHook = hooksProvider?.getHook(ControllerViewContextHook)
        if (controllerViewCtxHook == null && themeId == null) return baseInflater
        var inflaterContext = baseInflater.context
        if (controllerViewCtxHook != null) {
            inflaterContext = controllerViewCtxHook.hook(environment, inflaterContext)
        }
        if (themeId != null) {
            inflaterContext = ContextThemeWrapper(inflaterContext, themeId)
        }
        return LayoutInflater.from(inflaterContext)
    }

    internal fun getOrCreateView(inflater: LayoutInflater): View {
        return if (created) {
            view
        } else {
            createView(inflater)
        }
    }

    protected fun ControllerContainer.applyEdgeToEdgeConfig() {
        val controllerFitsStatusBar = this@Controller.fitStatusBar
        val controllerFitsNavigationBar = this@Controller.fitNavigationBar
        if (controllerFitsStatusBar != null) {
            this.fitStatusBar = controllerFitsStatusBar
        }
        if (controllerFitsNavigationBar != null) {
            this.fitNavigationBar = controllerFitsNavigationBar
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    internal fun bind(
        controllerManager: ControllerManager,
        parentController: Controller?,
        enterTransition: TransitionAnimation,
    ) {
        bind(controllerManager, parentController)
        this.environment.enterTransition = enterTransition
    }

    internal fun bind(
        controllerManager: ControllerManager,
        parentController: Controller?,
    ) {
        parentControllerManager = controllerManager
        this.parentController = parentController
    }

    open fun onCreate() {
        _created = true
        controllerExtensions.forEach { extension -> extension.init(attachedScope) }
        onCreateCallbacks.forEach { func -> func() }
        onCreateCallbacks.clear()

        controllersCache.onControllerCreated(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _started = true

        controllerExtensions.forEach { extension -> extension.onParentLifecycleEvent(Lifecycle.Event.ON_CREATE) }
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback -> callback.onControllerCreated(this) }
    }

    open fun onDestroy() {
        if (destroyed) return
        _destroyed = true
        onDestroyCallbacks.forEach { func -> func() }
        onDestroyCallbacks.clear()
        createdScope.coroutineContext.cancelChildren()
        controllersCache.onControllerDestroyed(this)
        if (_started) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            _started = false
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        controllerExtensions.forEach { extension -> extension.onParentLifecycleEvent(Lifecycle.Event.ON_DESTROY) }
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback -> callback.onControllerDestroyed(this) }
    }

    open fun onAttach() {
        _attached = true
        _detached = false

        onAttachCallbacks.forEach { func -> func() }
        onAttachCallbacks.clear()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        KompotPlugin.controllerShownSharedFlow.tryEmit(this)

        controllerExtensions.forEach { extension -> extension.onParentLifecycleEvent(Lifecycle.Event.ON_RESUME) }
    }

    open fun onDetach() {
        attachedScope.coroutineContext.cancelChildren()
        _attached = false
        _detached = true
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        controllerExtensions.forEach { extension -> extension.onParentLifecycleEvent(Lifecycle.Event.ON_PAUSE) }
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback -> callback.onControllerDetached(this) }
    }

    internal fun finish() {
        if (!detached) {
            onDetach()
        }
        onDestroy()
    }

    open fun onHostResumed() = Unit

    open fun onHostPaused() = Unit

    open fun onHostStarted() {
        if (!_started) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
    }

    open fun onHostStopped() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        _started = false
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        doOnAttach {
            controllerExtensions.forEach { extension ->
                extension.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        controllerExtensions.forEach { extension ->
            extension.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onTransitionRunUp(enter: Boolean) = Unit

    override fun onTransitionStart(enter: Boolean) {
        _activeTransition = if (enter) ActiveTransition.ENTER else ActiveTransition.EXIT
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback ->
            callback.onTransitionStart(this, enter)
        }
    }

    override fun onTransitionEnd(enter: Boolean) {
        _activeTransition = ActiveTransition.NONE
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback ->
            callback.onTransitionEnd(this, enter)
        }
        if (!enter) {
            onExitTransitionEndCallbacks.forEach { func -> func() }
            onExitTransitionEndCallbacks.clear()
        }
    }

    override fun onTransitionCanceled() {
        _activeTransition = ActiveTransition.NONE
    }

    open fun handleBack(): Boolean {
        if (controllerExtensions.any { extension -> extension.handleBack() }) {
            return true
        }
        if (!backEnabled) {
            return true
        }
        return false
    }

    internal open fun onParentManagerCleared() {
        controllerExtensions.any { extension -> extension.handleBack() }
    }

    fun getTopFlow(): RootFlow<*, *> {
        var topController = (this as? BaseFlow<*, *, *>) ?: parentController
        while (topController?.parentController != null) {
            topController = topController.parentController
        }
        return topController as RootFlow<*, *>
    }

    override fun startActivity(intent: Intent) =
        activityLauncher.startActivity(intent)

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) =
        activityLauncher.startActivityForResult(intent, requestCode, options)

    override fun requestPermissions(permissions: List<String>, requestCode: Int) =
        permissionsRequester.requestPermissions(permissions, requestCode)

    protected fun <T> Flow<T>.collectTillDetachView(
        onError: suspend (Throwable) -> Unit = { Timber.e(it) },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job = collectTillDetachView(
        attached = attached,
        detached = detached,
        attachedScope = attachedScope,
        onError = onError,
        onSuccessCompletion = onSuccessCompletion,
        onEach = onEach
    )

    fun doOnCreate(onCreate: () -> Unit) {
        if (created) {
            onCreate()
        } else {
            onCreateCallbacks.add(onCreate)
        }
    }

    fun doOnAttach(onAttach: () -> Unit) {
        if (attached) {
            onAttach()
        } else {
            onAttachCallbacks.add(onAttach)
        }
    }

    fun doOnNextExitTransition(onNextExit: () -> Unit) {
        onExitTransitionEndCallbacks.add(onNextExit)
    }

    fun doOnDestroy(onDestroy: () -> Unit) {
        if (destroyed) {
            onDestroy()
        } else {
            onDestroyCallbacks.add(onDestroy)
        }
    }

    internal open fun handleQuit() {
        parentController?.handleQuit()
    }

    enum class ActiveTransition {
        ENTER,
        EXIT,
        NONE
    }
}