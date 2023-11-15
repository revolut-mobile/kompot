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

package com.revolut.kompot.holder

import android.view.View
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.TransitionListener
import com.revolut.kompot.view.ControllerContainer

internal class ControllerTransaction(
    val from: Controller?,
    val to: Controller?,
    val controllerManager: ControllerManager,
    val backward: Boolean,
    val indefinite: Boolean,
) : TransitionListener {

    private val controllerContainer
        get() = controllerManager.controllerViewHolder.container as? ControllerContainer

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
        if (controllerManager.attached) {
            updateControllersVisibilityLifecycle(
                controllerToDetach = from.takeIf { !indefinite },
                controllerToAttach = to,
            )
        }
    }

    private fun updateControllersVisibilityLifecycle(
        controllerToAttach: Controller?,
        controllerToDetach: Controller?
    ) {
        if (controllerToAttach == null && controllerToDetach == null) return
        require(controllerToAttach != controllerToDetach)

        val shouldDetachController = controllerToDetach?.attached == true
        val shouldAttachController = controllerToAttach?.attached == false

        if (shouldDetachController) {
            controllerToDetach?.onDetach()
        }
        if (shouldAttachController) {
            controllerToAttach?.onAttach()
        }

        if (shouldDetachController && controllerToDetach != null) {
            controllerManager.onDetachController?.invoke(controllerToDetach, controllerManager)
        }
        if (shouldAttachController && controllerToAttach != null) {
            controllerManager.onAttachController?.invoke(controllerToAttach, controllerManager)
        }
    }

    override fun onTransitionStart() {
        controllerContainer?.onControllersTransitionStart(indefinite)
        from?.onTransitionStart(false)
        to?.onTransitionStart(true)
    }

    override fun onTransitionEnd() {
        controllerContainer?.onControllersTransitionEnd(indefinite)
        from?.onTransitionEnd(false)
        to?.onTransitionEnd(true)
    }

    override fun onTransitionFinished() {
        updateControllersVisibilityLifecycle(
            controllerToDetach = from.takeIf { indefinite },
            controllerToAttach = null,
        )

        if (from != null) {
            controllerManager.controllerViewHolder.remove(from.view)
            if (backward || !controllerManager.controllersCache.isControllerCached(from.key)) {
                from.onDestroy()
            }
        }
    }

    override fun onTransitionCanceled() {
        controllerContainer?.onControllersTransitionCanceled(indefinite)
        to?.onTransitionCanceled()
        from?.onTransitionCanceled()

        //revert critical state: update controllers lifecycle and detach views if needed
        updateControllersVisibilityLifecycle(
            controllerToDetach = to,
            controllerToAttach = from,
        )
        if (to != null) {
            controllerManager.controllerViewHolder.remove(to.view)
            if (!backward) {
                to.onDestroy()
            }
        }
        controllerManager.onTransitionCanceled(
            from = from,
            backward = backward,
        )
    }

    companion object {

        fun replaceTransaction(
            from: Controller?,
            to: Controller,
            controllerManager: ControllerManager,
            backward: Boolean,
            indefinite: Boolean,
        ) = ControllerTransaction(
            from = from,
            to = to,
            controllerManager = controllerManager,
            backward = backward,
            indefinite = indefinite,
        )

        fun popTransaction(
            from: Controller,
            controllerManager: ControllerManager,
        ) = ControllerTransaction(
            from = from,
            to = null,
            controllerManager = controllerManager,
            backward = true,
            indefinite = false,
        )

        fun removeTransaction(
            from: Controller,
            controllerManager: ControllerManager
        ) = ControllerTransaction(
            from = from,
            to = null,
            controllerManager = controllerManager,
            backward = false,
            indefinite = false,
        )
    }

}