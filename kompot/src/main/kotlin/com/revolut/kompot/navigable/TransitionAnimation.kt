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

package com.revolut.kompot.navigable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface TransitionAnimation : Parcelable {

    val indefinite: Boolean get() = false

    @Parcelize
    object NONE : TransitionAnimation

    @Parcelize
    object SLIDE_RIGHT_TO_LEFT : TransitionAnimation

    @Parcelize
    object SLIDE_LEFT_TO_RIGHT : TransitionAnimation

    @Parcelize
    object FADE : TransitionAnimation

    @Parcelize
    object MODAL_FADE : TransitionAnimation

    @Parcelize
    object MODAL_SLIDE : TransitionAnimation

    @Parcelize
    object BOTTOM_DIALOG_SLIDE : TransitionAnimation

    interface Custom : TransitionAnimation {
        val providerId: Int
    }
}