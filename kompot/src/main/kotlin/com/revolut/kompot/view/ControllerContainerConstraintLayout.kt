package com.revolut.kompot.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import com.revolut.kompot.R

class ControllerContainerConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ControllerContainer {
    private var transitionActive: Boolean = false
    private var lastTransitionStartTime = 0L

    override var fitStatusBar = false
        set(value) {
            FitStatusBarDelegate.setFitStatusBarProperty(value, this)
            field = value
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ControllerContainer).use {
            fitStatusBar = it.getBoolean(R.styleable.ControllerContainer_fitStatusBar, fitStatusBar)
        }
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets = FitStatusBarDelegate
        .dispatchApplyWindowInsets(insets, fitStatusBar, this, { super.dispatchApplyWindowInsets(it) }, { super.onApplyWindowInsets(it) })

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean = transitionActive || (System.currentTimeMillis() - lastTransitionStartTime <= 200) ||
            super.onInterceptTouchEvent(event)

    override fun onTransitionRunUp(enter: Boolean) {
        lastTransitionStartTime = System.currentTimeMillis()
    }

    override fun onTransitionStart(enter: Boolean) {
        transitionActive = true
        lastTransitionStartTime = System.currentTimeMillis()
    }

    override fun onTransitionEnd(enter: Boolean) {
        transitionActive = false
    }
}