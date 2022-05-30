package com.revolut.kompot.view

import android.content.Context
import android.graphics.Insets
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout

internal class RootFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        fitsSystemWindows = true
    }

    /**
     * windowSoftInputMode="adjustResize" and transparent statusbar fix.
     *
     * https://stackoverflow.com/questions/21092888/windowsoftinputmode-adjustresize-not-working-with-translucent-action-navbar
     */
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        super.onApplyWindowInsets(
            insets.replaceSystemWindowInsets(
                0, 0, 0,
                insets.systemWindowInsetBottom
            )
        )

        //we should return non consumed WindowInsets to forward it to children
        return insets
    }
}