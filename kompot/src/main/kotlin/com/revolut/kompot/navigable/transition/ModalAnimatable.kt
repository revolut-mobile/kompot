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

package com.revolut.kompot.navigable.transition

import android.view.View

interface ModalAnimatable {

    val view: View

    var style: Style

    fun show(onTransitionEnd: () -> Unit)

    fun hide(onTransitionEnd: () -> Unit)

    fun addContent(view: View)

    fun removeContent(view: View)

    fun setOnDismissListener(onDismiss: (() -> Unit)?)

    enum class Style {
        FADE, SLIDE, BOTTOM_DIALOG_SHEET,
    }
}