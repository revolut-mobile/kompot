package com.revolut.kompot.view

import com.revolut.kompot.navigable.transition.TransitionCallbacks

interface ControllerContainer : TransitionCallbacks {
    var fitStatusBar: Boolean

    companion object {
        internal val STATUS_BAR_HEIGHT
            get() = FitStatusBarDelegate.statusBarHeight
    }
}