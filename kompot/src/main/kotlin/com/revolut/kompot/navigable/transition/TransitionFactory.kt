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

import com.revolut.kompot.navigable.ModalTransitionAnimation
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.transition.Transition.Companion.DURATION_DEFAULT

internal class TransitionFactory {

    fun createTransition(animation: TransitionAnimation): Transition = when (animation) {
        TransitionAnimation.NONE -> ImmediateTransition()
        TransitionAnimation.SLIDE_RIGHT_TO_LEFT -> SlideTransition(DURATION_DEFAULT, SlideTransition.Direction.RIGHT_TO_LEFT)
        TransitionAnimation.SLIDE_LEFT_TO_RIGHT -> SlideTransition(DURATION_DEFAULT, SlideTransition.Direction.LEFT_TO_RIGHT)
        TransitionAnimation.FADE -> FadeTransition(DURATION_DEFAULT)
        is TransitionAnimation.Custom -> CustomTransition(animation)
        is ModalTransitionAnimation.ModalPopup -> ModalShiftTransition(ModalAnimatable.Style.POPUP, animation.showImmediately)
        is ModalTransitionAnimation.ModalFullscreenFade -> ModalShiftTransition(ModalAnimatable.Style.FULLSCREEN_FADE, animation.showImmediately)
        is ModalTransitionAnimation.ModalFullscreenSlideFromBottom -> ModalShiftTransition(ModalAnimatable.Style.FULLSCREEN_SLIDE_FROM_BOTTOM, animation.showImmediately)
        is ModalTransitionAnimation.BottomDialog -> ModalShiftTransition(ModalAnimatable.Style.BOTTOM_DIALOG_SHEET, animation.showImmediately)
    }
}