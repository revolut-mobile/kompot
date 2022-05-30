package com.revolut.kompot.holder

import android.view.View
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.TransitionListener

internal class ControllerTransaction(
    val from: Controller?,
    val to: Controller?,
    val controllerManager: ControllerManager,
    val backward: Boolean
) : TransitionListener {

    fun startWith(animation: TransitionAnimation) {
        to?.onTransitionRunUp(true)
        from?.onTransitionRunUp(false)
        controllerManager.controllerViewHolder.makeTransition(
            from = from?.view,
            to = to?.view,
            animation = animation,
            backward = backward,
            transitionListener = this
        )
    }

    override fun onTransitionCreated() {
        from?.view?.visibility = View.VISIBLE
        to?.view?.visibility = View.VISIBLE

        from?.onDetach()
        val managerAttached = controllerManager.attached
        val destinationControllerAttached = to?.attached == true
        if (managerAttached && !destinationControllerAttached) {
            to?.onAttach()
        }

        if (from != null) {
            controllerManager.onDetachController?.invoke(from, controllerManager)
        }

        if (to != null && managerAttached && !destinationControllerAttached) {
            controllerManager.onAttachController?.invoke(to, controllerManager)
        }
    }

    override fun onTransitionStart() {
        from?.onTransitionStart(false)
        to?.onTransitionStart(true)
    }

    override fun onTransitionEnd() {
        from?.onTransitionEnd(false)
        to?.onTransitionEnd(true)
    }

    override fun onTransitionFinished() {
        if (from != null) {
            controllerManager.controllerViewHolder.remove(from.view)
            if (backward || !controllerManager.controllersCache.isControllerCached(from.key)) {
                from.onDestroy()
            }
        }
    }

    companion object {

        fun replaceTransaction(
            from: Controller?,
            to: Controller,
            controllerManager: ControllerManager,
            backward: Boolean
        ) = ControllerTransaction(
            from = from,
            to = to,
            controllerManager = controllerManager,
            backward = backward
        )

        fun popTransaction(
            from: Controller,
            controllerManager: ControllerManager
        ) = ControllerTransaction(
            from = from,
            to = null,
            controllerManager = controllerManager,
            backward = true,
        )

        fun removeTransaction(
            from: Controller,
            controllerManager: ControllerManager
        ) = ControllerTransaction(
            from = from,
            to = null,
            controllerManager = controllerManager,
            backward = false,
        )

    }

}