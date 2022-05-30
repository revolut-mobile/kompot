package com.revolut.kompot.holder

import android.view.View
import android.view.ViewGroup
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.TransitionListener

/**
 * Behaves like a DefaultControllerViewHolder
 * but with a possibility to swap the underlying
 * container ViewGroup
 */
internal class RootControllerViewHolder : ControllerViewHolder {

    private var controllerViewHolder: DefaultControllerViewHolder? = null

    private var _container: ViewGroup? = null
        set(value) {
            controllerViewHolder = value?.let { DefaultControllerViewHolder(it)}
            field = value
        }

    override val container: ViewGroup
        get() = requireNotNull(_container)

    fun setContainer(container: ViewGroup) {
        this._container = container
    }

    fun removeContainer() {
        this._container = null
    }

    override fun add(view: View) {
        controllerViewHolder?.add(view)
    }

    override fun makeTransition(
        from: View?,
        to: View?,
        animation: TransitionAnimation,
        backward: Boolean,
        transitionListener: TransitionListener
    ) {
        controllerViewHolder?.makeTransition(from, to, animation, backward, transitionListener)
    }

    override fun remove(view: View) {
        controllerViewHolder?.remove(view)
    }

    override fun setOnDismissListener(onDismiss: () -> Unit) {
        controllerViewHolder?.setOnDismissListener(onDismiss)
    }

}