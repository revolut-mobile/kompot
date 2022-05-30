package com.revolut.kompot.view

import android.view.View
import android.view.WindowInsets
import androidx.core.view.updatePadding

internal object FitStatusBarDelegate {
    var statusBarHeight = 0

    fun setFitStatusBarProperty(value: Boolean, view: View) {
        if (value && statusBarHeight != 0) {
            view.updatePadding(top = statusBarHeight)
        }
    }

    fun dispatchApplyWindowInsets(
        insets: WindowInsets, fitStatusBar: Boolean, view: View,
        superDispatchApplyWindowInsets: (WindowInsets) -> WindowInsets,
        superOnApplyWindowInsets: (WindowInsets) -> WindowInsets
    ): WindowInsets {
        if (insets.systemWindowInsetTop != 0) {
            statusBarHeight = insets.systemWindowInsetTop
        }

        if (!fitStatusBar) {
            return superDispatchApplyWindowInsets(insets)
        }

        if (fitStatusBar && view.paddingTop == 0 && statusBarHeight != 0) {
            view.updatePadding(top = statusBarHeight)
        }

        return superOnApplyWindowInsets(
            insets.replaceSystemWindowInsets(
                0, 0, 0,
                insets.systemWindowInsetBottom
            )
        )
    }
}