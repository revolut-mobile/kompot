package com.revolut.kompot.holder

import android.view.View
import android.view.ViewGroup
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.TransitionListener

internal interface ControllerViewHolder {

    val container: ViewGroup

    fun add(view: View)

    /**
     * Runs transition form the views specified in params
     * Views should be added to the holder before calling this method
     */
    fun makeTransition(
        from: View?,
        to: View?,
        animation: TransitionAnimation,
        backward: Boolean,
        transitionListener: TransitionListener
    )

    fun remove(view: View)

    fun setOnDismissListener(onDismiss: () -> Unit)
}