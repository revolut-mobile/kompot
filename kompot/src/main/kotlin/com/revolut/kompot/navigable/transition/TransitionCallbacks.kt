package com.revolut.kompot.navigable.transition

interface TransitionCallbacks {
    fun onTransitionRunUp(enter: Boolean)

    fun onTransitionStart(enter: Boolean)

    fun onTransitionEnd(enter: Boolean)
}