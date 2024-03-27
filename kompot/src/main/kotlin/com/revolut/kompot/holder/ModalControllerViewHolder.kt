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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.ModalAnimatable
import com.revolut.kompot.navigable.transition.Transition
import com.revolut.kompot.navigable.transition.TransitionFactory
import com.revolut.kompot.navigable.transition.TransitionListener
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.RootFrameLayout
import timber.log.Timber

internal class ModalControllerViewHolder(
    override val container: ViewGroup,
    private val modalAnimatable: (context: Context) -> ModalAnimatable
) : ControllerViewHolder {

    private val transitionFactory = TransitionFactory()
    private var activeTransition: Transition? = null
    private var onDismiss: (() -> Unit)? = null

    init {
        if (!(container is ControllerContainer || container is RootFrameLayout)) {
            throw IllegalStateException("Controller container should be ControllerContainer")
        }
    }

    override fun add(view: View) {
        val modalContainer = modalAnimatable(container.context)
        modalContainer.addContent(view)
        view.tag = modalContainer

        modalContainer.setOnDismissListener {
            onDismiss?.invoke()
        }
        activeTransition?.endImmediately()
        container.removeView(modalContainer.view)
        container.addView(
            modalContainer.view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun addToBottom(view: View) {
        Timber.w(
            "addToBottom has no effect on the modal controller view holder because " +
                    "it's always expected to have one view only"
        )
        add(view)
    }

    override fun makeTransition(
        from: View?,
        to: View?,
        animation: TransitionAnimation,
        backward: Boolean,
        transitionListener: TransitionListener
    ) {
        activeTransition?.endImmediately()
        activeTransition = transitionFactory.createTransition(animation)
        activeTransition?.start(
            from = from,
            to = to,
            backward = backward,
            transitionListener = transitionListener
        )
    }

    override fun remove(view: View) {
        (view.tag as? ModalAnimatable)?.let {
            it.removeContent(view)
            it.setOnDismissListener(null)
            container.removeView(it.view)
        }
    }

    override fun setOnDismissListener(onDismiss: () -> Unit) {
        this.onDismiss = onDismiss
    }
}