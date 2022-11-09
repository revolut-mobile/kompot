/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.LinearLayout
import com.revolut.kompot.R

@SuppressLint("CustomViewStyleable")
open class ControllerContainerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ControllerContainer {
    private var transitionActive: Boolean = false
    private var lastTransitionStartTime = 0L

    final override var fitStatusBar = false
    final override var fitNavigationBar = true

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ControllerContainer)
        fitStatusBar = ta.getBoolean(R.styleable.ControllerContainer_fitStatusBar, fitStatusBar)
        fitNavigationBar = ta.getBoolean(R.styleable.ControllerContainer_fitNavigationBar, fitNavigationBar)
        ta.recycle()
    }

    override fun onAttachedToWindow() {
        EdgeToEdgeViewDelegate.onViewAttachedToWindow(this)
        super.onAttachedToWindow()
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets =
        EdgeToEdgeViewDelegate.dispatchApplyWindowInsets(this, insets)

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