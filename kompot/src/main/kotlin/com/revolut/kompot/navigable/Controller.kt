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
import com.revolut.kompot.BuildConfig
import com.revolut.kompot.common.ActivityFromControllerLauncher
import com.revolut.kompot.common.ActivityLauncher
import com.revolut.kompot.common.PermissionsFromControllerRequester
import com.revolut.kompot.common.PermissionsRequester
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.transition.TransitionCallbacks
import com.revolut.kompot.utils.ControllerScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Suppress("SyntheticAccessor")
abstract class Controller : TransitionCallbacks, LifecycleOwner, ActivityLauncher, PermissionsRequester {
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

    internal val key: ControllerKey by lazy(LazyThreadSafetyMode.NONE) {
        keyInitialization()
    }
    open var keyInitialization: () -> ControllerKey = { ControllerKey.random() }

    internal val createdScope: CoroutineScope = ControllerScope()
    internal val attachedScope: CoroutineScope = ControllerScope()

    val resources: Resources
        get() = activity.resources

    internal lateinit var parentControllerManager: ControllerManager
    protected val controllersCache: ControllersCache
        get() = parentControllerManager.controllersCache
    internal val hooksProvider: HooksProvider?
        get() = parentControllerManager.hooksProvider

    private val activityLauncher by lazy(LazyThreadSafetyMode.NONE) {
        ActivityFromControllerLauncher(this)
    }
    private val permissionsRequester by lazy(LazyThreadSafetyMode.NONE) {
        PermissionsFromControllerRequester(this)
    }

    @StyleRes
    protected open val themeId: Int? = null
    protected abstract val layoutId: Int
    open val fitStatusBar = false

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

    open val controllerDelegates: Set<ControllerExtension> = emptySet()

    abstract fun createView(inflater: LayoutInflater): View

    internal fun patchLayoutInflaterWithTheme(inflater: LayoutInflater) = themeId?.let { themeResId ->
        LayoutInflater.from(ContextThemeWrapper(inflater.context, themeResId))
    } ?: inflater

    internal fun getOrCreateView(inflater: LayoutInflater): View {
        return if (created) {
            view
        } else {
            createView(inflater)
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    internal fun bind(
        controllerManager: ControllerManager,
        parentController: Controller?
    ) {
        parentControllerManager = controllerManager
        this.parentController = parentController
    }

    open fun onCreate() {
        _created = true
        onCreateCallbacks.forEach { func -> func() }
        onCreateCallbacks.clear()

        controllersCache.onControllerCreated(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _started = true

        controllerDelegates.forEach(ControllerExtension::onCreate)
    }

    open fun onDestroy() {
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

        controllerDelegates.forEach(ControllerExtension::onDestroy)
    }

    open fun onAttach() {
        _attached = true
        _detached = false

        onAttachCallbacks.forEach { func -> func() }
        onAttachCallbacks.clear()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        controllerDelegates.forEach(ControllerExtension::onAttach)
    }

    open fun onDetach() {
        attachedScope.coroutineContext.cancelChildren()
        _attached = false
        _detached = true
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        controllerDelegates.forEach(ControllerExtension::onDetach)
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
            controllerDelegates.forEach { delegate ->
                delegate.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        controllerDelegates.forEach { delegate ->
            delegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onTransitionRunUp(enter: Boolean) = Unit

    override fun onTransitionStart(enter: Boolean) {
        _activeTransition = if (enter) ActiveTransition.ENTER else ActiveTransition.EXIT
    }

    override fun onTransitionEnd(enter: Boolean) {
        _activeTransition = ActiveTransition.NONE
    }

    open fun handleBack(): Boolean {
        if (controllerDelegates.any { delegate -> delegate.handleBack() }) {
            return true
        }
        if (!backEnabled) {
            return true
        }
        return false
    }

    internal open fun onParentManagerCleared() {
        controllerDelegates.any { delegate -> delegate.handleBack() }
    }

    fun getTopFlow(): BaseFlow<*, *, *> {
        var topController = (this as? BaseFlow<*, *, *>) ?: parentController
        while (topController?.parentController != null) {
            topController = topController.parentController
        }
        return topController as BaseFlow<*, *, *>
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
    ): Job {
        if (BuildConfig.DEBUG) {
            when {
                detached -> error("collectTillDetachView is called after onDetach [${this@Controller}]")
                !attached -> error("collectTillDetachView is called before onAttach [${this@Controller}]")
            }
        }
        return launchInScope(
            scope = attachedScope,
            onError = onError,
            onSuccessCompletion = onSuccessCompletion,
            onEach = onEach
        )
    }

    private fun <T> Flow<T>.launchInScope(
        scope: CoroutineScope,
        onError: suspend (Throwable) -> Unit = { Timber.e(it) },
        onSuccessCompletion: suspend () -> Unit = {},
        onEach: suspend (T) -> Unit = {}
    ): Job =
        onEach(onEach)
            .onCompletion { cause ->
                if (cause == null) onSuccessCompletion()
            }
            .catch { cause ->
                onError(cause)
            }
            .launchIn(scope)

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