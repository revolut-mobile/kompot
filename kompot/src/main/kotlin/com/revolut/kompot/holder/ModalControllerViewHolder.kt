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