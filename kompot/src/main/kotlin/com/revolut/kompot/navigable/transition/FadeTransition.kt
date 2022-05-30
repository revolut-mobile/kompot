package com.revolut.kompot.navigable.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

internal class FadeTransition(
    duration: Long
) : AnimatorTransition(duration) {
    override fun getAnimator(from: View?, to: View?, backward: Boolean): Animator {
        val animator = AnimatorSet()
        if (to != null) {
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, 0f, 1f))
        }

        if (from != null) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f))
        }
        return animator
    }

    override fun startAnimationWhenViewIsReady(view: View, animation: () -> Unit) {
        animation()
    }
}