package com.revolut.kompot.navigable

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import com.revolut.kompot.KompotPlugin
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
        parentController: Controller?
    ) {
        val oldController = _activeController
        _activeController = controller

        controller.bind(this, parentController)

        val context = controllerViewHolder.container.context
        val controllerView = controller.getOrCreateView(LayoutInflater.from(context))
        controllerViewHolder.add(controllerView)
        if (!controller.created) {
            controller.onCreate()
        }

        ControllerTransaction.replaceTransaction(
            from = oldController.takeIf { oldController != _activeController },
            to = controller,
            controllerManager = this,
            backward = backward
        ).startWith(animation)

        KompotPlugin.controllerShownSharedFlow.tryEmit(controller)
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