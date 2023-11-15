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
import android.content.ContextWrapper
import android.content.Intent
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.core.view.contains
import com.revolut.kompot.holder.ControllerTransaction
import com.revolut.kompot.holder.ControllerViewHolder
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.hooks.HooksProvider

internal open class ControllerManager(
    val modal: Boolean,
    @LayoutRes internal val defaultFlowLayout: Int?,
    internal val controllersCache: ControllersCache,
    internal val controllerViewHolder: ControllerViewHolder,
    internal val onAttachController: ChildControllerListener? = null,
    internal val onDetachController: ChildControllerListener? = null,
    private val onTransitionCanceled: CanceledTransitionListener? = null
) {

    init {
        controllerViewHolder.setOnDismissListener {
            _activeController?.let { activeController ->
                val popTransaction = ControllerTransaction.popTransaction(
                    from = activeController,
                    controllerManager = this
                )
                popTransaction.startWith(TransitionAnimation.MODAL_SLIDE)
                _activeController = null
            }
        }
    }

    private var _activeController: Controller? = null
    val activeController: Controller?
        get() = _activeController

    val activity: Activity
        get() {
            val context = controllerViewHolder.container.context
            return if (context is Activity) {
                context
            } else {
                (context as ContextWrapper).baseContext as Activity
            }
        }

    private var _attached = true
    internal val attached: Boolean
        get() = _attached

    internal var hooksProvider: HooksProvider? = null

    internal fun showImmediately(controller: Controller) {
        show(controller, TransitionAnimation.NONE, false, null)
    }

    fun show(
        controller: Controller,
        animation: TransitionAnimation,
        backward: Boolean,
        parentController: Controller?,
    ) {
        val oldController = _activeController
        _activeController = controller

        controller.bind(this, parentController)

        val context = controllerViewHolder.container.context
        val controllerView = controller.getOrCreateView(LayoutInflater.from(context))
        if (controllerViewHolder.container.contains(controllerView).not() && controllerView.parent != null){
            val cache = controllersCache.getCacheLogWithKeys()
            // Throwing exception is okay since the app is going to crash anyway.
            throw IllegalStateException("Can’t show controller because it’s already attached to another flow. ${controller.fullControllerName} key: ${controller.key.value} \n $cache")
        }
        if (backward) {
            controllerViewHolder.addToBottom(controllerView)
        } else {
            controllerViewHolder.add(controllerView)
        }
        if (!controller.created) {
            controller.onCreate()
        }

        ControllerTransaction.replaceTransaction(
            from = oldController.takeIf { oldController != _activeController },
            to = controller,
            controllerManager = this,
            backward = backward,
            indefinite = animation.indefinite,
        ).startWith(animation)
    }

    internal fun onTransitionCanceled(from: Controller?, backward: Boolean) {
        _activeController = from
        onTransitionCanceled?.invoke(backward)
    }

    fun removeActiveController() {
        val controller = _activeController ?: return
        ControllerTransaction.removeTransaction(
            from = controller,
            controllerManager = this
        ).startWith(TransitionAnimation.NONE)
        resetActiveController()
    }

    open fun onDestroy() {
        _activeController?.onDestroy()
    }

    fun onHostResumed() {
        activeController?.onHostResumed()
    }

    fun onHostPaused() {
        activeController?.onHostPaused()
    }

    fun onHostStarted() {
        onAttach()
    }

    fun onHostStopped() {
        onDetach()
    }

    fun onAttach(): Boolean {
        if (_activeController != null) {
            _attached = true
            if (_activeController?.attached == false) {
                _activeController?.onAttach()

                return true
            }
        }

        return false
    }

    fun onDetach(): Boolean {
        if (_activeController != null) {
            _attached = false
            if (_activeController?.attached == true) {
                _activeController?.onDetach()

                return true
            }
        }

        return false
    }

    fun handleBack(): Boolean {
        if (_activeController == null) {
            return false
        }

        val handled = _activeController!!.handleBack()
        if (modal && !handled) {
            val popTransaction = ControllerTransaction.popTransaction(
                from = requireNotNull(_activeController),
                controllerManager = this
            )
            if (attached) {
                popTransaction.startWith(TransitionAnimation.MODAL_SLIDE)
            } else {
                popTransaction.startWith(TransitionAnimation.NONE)
                _attached = true
            }
            _activeController = null

            return true
        }

        return handled
    }

    internal fun clear() {
        _activeController?.onParentManagerCleared()

        if (modal && _activeController != null) {

            ControllerTransaction.popTransaction(
                from = requireNotNull(_activeController),
                controllerManager = this
            ).startWith(TransitionAnimation.MODAL_SLIDE)
            resetActiveController()
        }
    }

    private fun resetActiveController() {
        _activeController = null
        _attached = true
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = _activeController?.onActivityResult(requestCode, resultCode, data)

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        _activeController?.onRequestPermissionsResult(requestCode, permissions, grantResults)

}

internal typealias ChildControllerListener = (Controller, ControllerManager) -> Unit
internal typealias CanceledTransitionListener = (backward: Boolean) -> Unit