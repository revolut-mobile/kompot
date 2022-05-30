package com.revolut.kompot.holder

import android.view.View
import android.view.ViewGroup
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.Transition
import com.revolut.kompot.navigable.transition.TransitionFactory
import com.revolut.kompot.navigable.transition.TransitionListener
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.RootFrameLayout

internal class DefaultControllerViewHolder(
    override val container: ViewGroup
) : ControllerViewHolder {

    private val transitionFactory = TransitionFactory()
    private var activeTransition: Transition? = null

    init {
        if (!(container is ControllerContainer || container is RootFrameLayout)) {
            throw IllegalStateException("Controller's container must implement ControllerContainer interface")
        }
    }

    override fun add(view: View) {
        activeTransition?.endImmediately()
        container.removeView(view)
        container.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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
        container.removeView(view)
    }

    override fun setOnDismissListener(onDismiss: () -> Unit) {
        //Do nothing
    }
}