package com.revolut.kompot.navigable.transition

import com.revolut.kompot.navigable.TransitionAnimation

internal class TransitionFactory {

    fun createTransition(animation: TransitionAnimation): Transition = when (animation) {
        TransitionAnimation.NONE -> ImmediateTransition()
        TransitionAnimation.SLIDE_RIGHT_TO_LEFT -> SlideTransition(AnimatorTransition.DURATION_DEFAULT, SlideTransition.Direction.RIGHT_TO_LEFT)
        TransitionAnimation.SLIDE_LEFT_TO_RIGHT -> SlideTransition(AnimatorTransition.DURATION_DEFAULT, SlideTransition.Direction.LEFT_TO_RIGHT)
        TransitionAnimation.FADE -> FadeTransition(AnimatorTransition.DURATION_DEFAULT)
        TransitionAnimation.MODAL_SLIDE -> ModalShiftTransition(ModalAnimatable.Style.SLIDE)
        TransitionAnimation.MODAL_FADE -> ModalShiftTransition(ModalAnimatable.Style.FADE)
    }

}