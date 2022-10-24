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

import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding

internal object EdgeToEdgeViewDelegate {

    fun onViewAttachedToWindow(containerView: View) {
        ViewCompat.requestApplyInsets(containerView)
    }

    fun dispatchApplyWindowInsets(controllerContainer: ControllerContainer, insets: WindowInsets): WindowInsets {
        val handledInsets = controllerContainer.handleInsets(insets) ?: insets
        if (handledInsets.isConsumed) {
            return handledInsets
        }
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

}