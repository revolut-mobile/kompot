package com.revolut.kompot.navigable.transition

internal interface TransitionListener {

    fun onTransitionCreated()

    fun onTransitionStart()

    fun onTransitionEnd()

    fun onTransitionFinished()

}