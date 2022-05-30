package com.revolut.kompot.navigable.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import androidx.core.view.doOnLayout

internal class SlideTransition(
    duration: Long,
    private val direction: Direction
) : AnimatorTransition(duration) {

    override fun getAnimator(from: View?, to: View?, backward: Boolean): Animator {
        val animator = AnimatorSet()
        if (to != null) {
            val width = to.width.toFloat()
            val initial = when (direction) {
                Direction.RIGHT_TO_LEFT -> if (backward) -width else width
                Direction.LEFT_TO_RIGHT -> if (backward) width else -width
            }

            animator.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, initial, 0f))
        }

        if (from != null) {
            val width = from.width.toFloat()
            val target = when (direction) {
                Direction.RIGHT_TO_LEFT -> if (backward) width else -width
                Direction.LEFT_TO_RIGHT -> if (backward) -width else width
            }

            animator.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0f, target))
        }
        return animator
    }

    override fun startAnimationWhenViewIsReady(view: View, animation: () -> Unit) {
        if (view.width > 0) {
            animation()
        } else {
            view.doOnLayout { animation() }
        }
    }

    enum class Direction {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT
    }
}