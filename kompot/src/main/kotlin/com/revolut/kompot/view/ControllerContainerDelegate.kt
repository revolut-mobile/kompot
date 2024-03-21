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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.revolut.kompot.R

class ControllerContainerDelegate(context: Context, attrs: AttributeSet?) : ControllerContainer {

    override var fitStatusBar = false
    override var fitNavigationBar = true
    override var containerId: String = ControllerContainer.NO_CONTAINER_ID
    override var insetsInterceptor: ((View, WindowInsets) -> WindowInsets)? = null
    private var activeTransition: ActiveTransition? = null
    override val controllersTransitionActive: Boolean get() = activeTransition != null

    private var _latestDispatchedInsets: WindowInsets? = null
    override val latestDispatchedInsets: WindowInsets?
        get() = _latestDispatchedInsets

    private var savedStateDispatchAllowed: Boolean = false

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ControllerContainer)
        fitStatusBar = ta.getBoolean(R.styleable.ControllerContainer_fitStatusBar, fitStatusBar)
        fitNavigationBar = ta.getBoolean(R.styleable.ControllerContainer_fitNavigationBar, fitNavigationBar)
        ta.recycle()
    }

    override fun handleViewAttachedToWindow(containerView: View) {
        ViewCompat.requestApplyInsets(containerView)
    }

    override fun handleDispatchApplyWindowInsets(controllerContainer: ControllerContainer, insets: WindowInsets): WindowInsets {
        val interceptedInsets = insetsInterceptor?.invoke(controllerContainer.asViewGroup(), insets)
        if (interceptedInsets != null && interceptedInsets.isConsumed) {
            return interceptedInsets
        }
        val handledInsets = controllerContainer.handleInsets(interceptedInsets ?: insets) ?: insets
        if (handledInsets.isConsumed) {
            return handledInsets
        }
        _latestDispatchedInsets = handledInsets
        return dispatchInsetsToChildren(controllerContainer.asViewGroup(), handledInsets)
    }

    /**
     * On the older versions of android (<30) there is a broken logic for propagating insets to children
     * This method applies a correct behaviour so it is available in all the versions
     * https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/view/ViewGroup.java;l=7341-7371;drc=6411c81462e3594c38a1be5d7c27d67294139ab8
     */
    private fun dispatchInsetsToChildren(containerView: ViewGroup, insets: WindowInsets): WindowInsets {
        containerView.children.forEach { child ->
            child.dispatchApplyWindowInsets(insets)
        }
        return insets
    }

    private fun ControllerContainer.handleInsets(insets: WindowInsets): WindowInsets? {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val handledInsetsType = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        val insetDimensions = compatInsets.getInsets(handledInsetsType)

        val containerView = asViewGroup()

        containerView.updatePadding(
            top = if (fitStatusBar) insetDimensions.top else 0,
            bottom = if (fitNavigationBar) insetDimensions.bottom else 0,
        )

        val newInsets = Insets.of(
            insetDimensions.left,
            if (fitStatusBar) 0 else insetDimensions.top,
            insetDimensions.right,
            if (fitNavigationBar) 0 else insetDimensions.bottom,
        )

        return WindowInsetsCompat.Builder(compatInsets)
            .setInsets(handledInsetsType, newInsets)
            .build()
            .toWindowInsets()
    }

    private fun ControllerContainer.asViewGroup() = this as ViewGroup

    override fun handleDispatchTouchEvent(ev: MotionEvent): Boolean {
        val activeTransition = activeTransition
        return activeTransition != null && !activeTransition.indefinite
    }

    /**
     * Kompot has its own solution for storing views state. It creates a separate bundle for every controller
     * and keeps views state in this bundle. To avoid duplicated data in the saved state, we'll need to ensure that:
     *  - Controller keeps the state of its own views only (e.g flows don't keep view states of their children)
     *  - Activity doesn't persist state of the controller views
     *
     *  In order to achieve that, we control whether container saved state is allowed using [savedStateDispatchAllowed] flag. Controller containers handle
     *  this flag to understand if the save state instruction came from the owning controller and not from the
     *  android framework. Controller containers handle saved state only when the [savedStateDispatchAllowed] flag is enabled.
     */
    override fun allowSavedStateDispatch() {
        savedStateDispatchAllowed = true
    }

    /**
     * Use [savedStateDispatchAllowed] flag to determine if saved state is allowed. Controllers use this flag
     * to check if it's allowed to call methods like [View.dispatchSaveInstanceState].
     * If the flag is enabled, we'll clear it right away to ensure the flag is clean for the next saved state invocations.
     * [View.dispatchSaveInstanceState] and [View.dispatchRestoreInstanceState] traverse all underlying views hierarchy, so using the flag helps us to stop traversal
     * when we reach the next controller container. More info in the [ControllerContainer.allowSavedStateDispatch],
     *
     * @return true if saved state dispatch is allowed and we can proceed with the saved state. false otherwise.
     */
    override fun useSavedStateDispatchAllowance(): Boolean =
        if (savedStateDispatchAllowed) {
            savedStateDispatchAllowed = false
            true
        } else {
            false
        }

    override fun onControllersTransitionStart(indefinite: Boolean) {
        activeTransition = ActiveTransition(indefinite)
    }

    override fun onControllersTransitionEnd(indefinite: Boolean) {
        activeTransition = null
    }

    override fun onControllersTransitionCanceled(indefinite: Boolean) {
        activeTransition = null
    }

    @JvmInline
    private value class ActiveTransition(val indefinite: Boolean)
}