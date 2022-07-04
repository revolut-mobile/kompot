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