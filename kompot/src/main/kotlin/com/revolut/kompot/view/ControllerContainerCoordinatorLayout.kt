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
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.WindowInsets
import androidx.coordinatorlayout.widget.CoordinatorLayout

@SuppressLint("CustomViewStyleable")
open class ControllerContainerCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), ControllerContainer by ControllerContainerDelegate(context, attrs) {

    override fun onAttachedToWindow() {
        handleViewAttachedToWindow(this)
        super.onAttachedToWindow()
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets =
        handleDispatchApplyWindowInsets(this, insets)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean =
        handleDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)

    override fun saveState(outState: SparseArray<Parcelable>) {
        allowSavedStateDispatch()
        dispatchSaveInstanceState(outState)
    }

    override fun restoreState(state: SparseArray<Parcelable>) {
        allowSavedStateDispatch()
        dispatchRestoreInstanceState(state)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        if (useSavedStateDispatchAllowance()) {
            super.dispatchSaveInstanceState(container)
        }
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        if (useSavedStateDispatchAllowance()) {
            super.dispatchRestoreInstanceState(container)
        }
    }
}