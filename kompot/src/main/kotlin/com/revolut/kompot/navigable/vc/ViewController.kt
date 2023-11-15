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

package com.revolut.kompot.navigable.vc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.postDelayed
import com.revolut.kompot.KompotPlugin
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.di.flow.ControllerComponent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.hooks.PersistentModelStateStorageHook
import com.revolut.kompot.navigable.hooks.LifecycleViewTagHook
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.navigable.utils.hideKeyboard
import com.revolut.kompot.navigable.utils.showKeyboard
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi
import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.view.ControllerContainer
import kotlinx.coroutines.Dispatchers

abstract class ViewController<OUTPUT : IOData.Output> : Controller(), ViewControllerApi {

    protected abstract val controllerModel: ViewControllerModelApi<OUTPUT>
    private val internalControllerModel get() =  controllerModel as ControllerModel

    protected val parentComponent: ControllerComponent
        get() = parentFlow.component

    internal var onResult: (data: OUTPUT) -> Unit = { }

    open val needKeyboard: Boolean = false

    abstract override val component: ViewControllerComponent
    override val controllerExtensions by lazy(LazyThreadSafetyMode.NONE) {
        component.getControllerExtensions()
    }

    override fun createView(inflater: LayoutInflater): View {
        val inflatedLayout = patchLayoutInflaterWithTheme(inflater).inflate(layoutId, null, false)
        require(inflatedLayout is ControllerContainer) { "$controllerName: root ViewGroup should be ControllerContainer" }
        inflatedLayout.applyEdgeToEdgeConfig()
        inflatedLayout.tag = this.controllerName
        hooksProvider?.getHook(LifecycleViewTagHook.Key)?.tagId?.let { lifecycleTag ->
            inflatedLayout.setTag(lifecycleTag, lifecycle)
        }
        return inflatedLayout.also { this.view = it }
    }

    final override fun onCreate() {
        super.onCreate()
        restorePersistentState()
        internalControllerModel.injectDependencies(
            dialogDisplayer = findRootFlow().rootDialogDisplayer,
            eventsDispatcher = this,
            controllersCache = controllersCache,
            mainDispatcher = Dispatchers.Main.immediate,
            controllerModelExtensions = component.getControllerModelExtensions(),
        )

        tillDestroyBinding += controllerModel.resultsBinder()
            .bind(::onPostScreenResult)
        tillDestroyBinding += controllerModel.backPressBinder()
            .bind { onPostBack() }
        modelBinding.onCreate()

        onCreated(view)
        internalControllerModel.onLifecycleEvent(LifecycleEvent.CREATED)
    }

    final override fun onDestroy() {
        super.onDestroy()
        onDestroyed()
        tillDestroyBinding.clear()
        internalControllerModel.onLifecycleEvent(LifecycleEvent.FINISHED)
        modelBinding.onDestroy()
    }

    final override fun onAttach() {
        super.onAttach()
        onShown(view)
        internalControllerModel.onLifecycleEvent(LifecycleEvent.SHOWN)
        modelBinding.onShow()
        if (needKeyboard) {
            view.postDelayed(400L) {
                if (attached) {
                    activity.currentFocus?.showKeyboard()
                } else {
                    activity.currentFocus?.clearFocus()
                    view.showKeyboard(400)
                }
            }
        } else {
            activity.currentFocus?.clearFocus()
            view.showKeyboard(400)
        }
        KompotPlugin.controllerLifecycleCallbacks.forEach { callback -> callback.onControllerAttached(this) }
    }

    final override fun onDetach() {
        super.onDetach()
        onHidden()
        internalControllerModel.onLifecycleEvent(LifecycleEvent.HIDDEN)
        modelBinding.onHide()
        savePersistentState()
    }

    override fun onTransitionStart(enter: Boolean) {
        super.onTransitionStart(enter)
        modelBinding.onTransitionStart(enter)
    }

    override fun onTransitionEnd(enter: Boolean) {
        super.onTransitionEnd(enter)
        modelBinding.onTransitionEnd(enter)
    }

    override fun onTransitionRunUp(enter: Boolean) {
        super.onTransitionRunUp(enter)

        if (enter && !needKeyboard) {
            activity.currentFocus?.hideKeyboard()
        }
    }

    override fun onHostPaused() {
        super.onHostPaused()
        modelBinding.onHostPaused()
    }

    override fun onHostResumed() {
        super.onHostResumed()
        modelBinding.onHostResumed()
    }

    override fun onHostStarted() {
        super.onHostStarted()
        onAttach()
        modelBinding.onHostStarted()
    }

    override fun onHostStopped() {
        super.onHostStopped()
        onDetach()
        modelBinding.onHostStopped()
    }

    override fun onParentManagerCleared() {
        modelBinding.onParentManagerCleared()
        super.onParentManagerCleared()
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(requestCode, resultCode, data)
        modelBinding.onActivityResult(requestCode, resultCode, data)
    }

    final override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleRequestPermissionsResult(requestCode, permissions, grantResults)
        modelBinding.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    open fun handleRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit

    override fun handleEvent(event: Event): EventResult? {
        if (event._controller == null) {
            event._controller = this
        }
        return internalControllerModel.tryHandleEvent(event) ?: (parentController as? EventsDispatcher)?.handleEvent(event)
    }

    private fun onPostScreenResult(result: OUTPUT) {
        Preconditions.requireMainThread("ViewController#postScreenResult")
        onResult.invoke(result)
    }

    private fun onPostBack() {
        Preconditions.requireMainThread("ViewController#postBack")
        getTopFlow().handleBack()
    }

    override fun handleBack(): Boolean = modelBinding.handleBack(
        defaultHandler = { super.handleBack() }
    )

    override fun handleQuit() {
        if (!modelBinding.handleQuit()) {
            super.handleQuit()
        }
    }

    final override fun saveState(outState: Bundle) {
        modelBinding.saveState(outState)
    }

    final override fun restoreState(state: Bundle) {
        modelBinding.restoreState(state)
    }

     private fun restorePersistentState() {
        hooksProvider
            ?.getHook(PersistentModelStateStorageHook.Key)
            ?.storage
            ?.let(modelBinding::restoreStateFromStorage)
    }

    private fun savePersistentState() {
        hooksProvider
            ?.getHook(PersistentModelStateStorageHook.Key)
            ?.storage
            ?.let(modelBinding::saveStateToStorage)
    }

    fun withResult(block: (OUTPUT) -> Unit): ViewController<OUTPUT> {
        this.onResult = block
        return this
    }

    fun postResult(result: OUTPUT) {
        this.onResult.invoke(result)
    }

    protected open fun onCreated(view: View) = Unit
    protected open fun onShown(view: View) = Unit
    protected open fun onHidden() = Unit
    protected open fun onDestroyed() = Unit
}