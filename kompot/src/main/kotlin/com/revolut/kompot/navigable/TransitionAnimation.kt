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
import com.revolut.kompot.ExperimentalBottomDialogStyle
import com.revolut.kompot.common.ModalDestination
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

    interface Custom : TransitionAnimation {
        val providerId: Int
    }
}

internal sealed interface InternalTransitionAnimation : TransitionAnimation

internal sealed interface ModalTransitionAnimation : InternalTransitionAnimation {

    val style: ModalDestination.Style

    @Parcelize
    data class ModalFullscreenFade(
        val showImmediately: Boolean = false,
        override val style: ModalDestination.Style
    ) : ModalTransitionAnimation

    @Parcelize
    data class ModalFullscreenSlideFromBottom(val showImmediately: Boolean = false) : ModalTransitionAnimation {
        override val style: ModalDestination.Style get() = ModalDestination.Style.FULLSCREEN_SLIDE_FROM_BOTTOM
    }

    @Parcelize
    data class ModalPopup(val showImmediately: Boolean = false) : ModalTransitionAnimation {
        override val style: ModalDestination.Style get() = ModalDestination.Style.POPUP
    }

    @Parcelize
    @OptIn(ExperimentalBottomDialogStyle::class)
    data class BottomDialog(val showImmediately: Boolean = false) : ModalTransitionAnimation {
        override val style: ModalDestination.Style get() = ModalDestination.Style.BOTTOM_DIALOG
    }
}

internal fun ModalDestination.Style.toModalTransitionAnimation(showImmediately: Boolean) =
    when (this) {
        ModalDestination.Style.POPUP -> ModalTransitionAnimation.ModalPopup(showImmediately)
        ModalDestination.Style.FULLSCREEN_FADE -> ModalTransitionAnimation.ModalFullscreenFade(showImmediately, this)
        ModalDestination.Style.FULLSCREEN_SLIDE_FROM_BOTTOM -> ModalTransitionAnimation.ModalFullscreenSlideFromBottom(showImmediately)
        ModalDestination.Style.FULLSCREEN_IMMEDIATE -> ModalTransitionAnimation.ModalFullscreenFade(showImmediately = true, this)
        ModalDestination.Style.BOTTOM_DIALOG -> ModalTransitionAnimation.BottomDialog(showImmediately)
    }

internal fun TransitionAnimation.extractModalStyle(): ModalDestination.Style? = (this as? ModalTransitionAnimation)?.style