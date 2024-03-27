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
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.FrameLayout

internal class RootFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Kompot has its own solution for storing views state. It creates a separate bundle for every controller
     * and keeps views state in this bundle. To avoid duplicated data in the saved state, we'll need to ensure that host
     * activity doesn't persist state of the controller views.
     *
     *  Root flow has empty implementation of the dispatchSaveInstanceState and dispatchRestoreInstanceState to ignore
     *  framework's native saved state.
     */
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) = Unit
    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) = Unit
}