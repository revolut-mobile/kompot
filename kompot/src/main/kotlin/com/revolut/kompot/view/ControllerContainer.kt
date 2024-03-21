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

import android.os.Parcelable
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import kotlinx.parcelize.Parcelize

interface ControllerContainer {
    var fitStatusBar: Boolean
    var fitNavigationBar: Boolean
    var containerId: String
    val controllersTransitionActive: Boolean
    val latestDispatchedInsets: WindowInsets?

    var insetsInterceptor: ((View, WindowInsets) -> WindowInsets)?

    fun handleViewAttachedToWindow(containerView: View)
    fun handleDispatchApplyWindowInsets(controllerContainer: ControllerContainer, insets: WindowInsets): WindowInsets
    fun handleDispatchTouchEvent(ev: MotionEvent): Boolean

    fun saveState(outState: SparseArray<Parcelable>) = Unit
    fun restoreState(state: SparseArray<Parcelable>) = Unit

    fun allowSavedStateDispatch()
    fun useSavedStateDispatchAllowance(): Boolean

    fun onControllersTransitionStart(indefinite: Boolean)
    fun onControllersTransitionEnd(indefinite: Boolean)
    fun onControllersTransitionCanceled(indefinite: Boolean)

    companion object {
        const val MAIN_CONTAINER_ID = "MAIN_CONTAINER_ID"
        const val MODAL_CONTAINER_ID = "MODAL_CONTAINER_ID"
        const val NO_CONTAINER_ID = "NO_CONTAINER_ID"
    }
}

@Parcelize
internal object DummyParcelable : Parcelable