package com.revolut.kompot.navigable.transition

import android.view.View

internal interface Transition {

    fun start(
        from: View?,
        to: View?,
        backward: Boolean,
        transitionListener: TransitionListener
    )

    fun endImmediately()
}