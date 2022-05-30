package com.revolut.kompot.navigable.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View

internal class ImmediateTransition : AnimatorTransition(duration = 0L, withAnimation = false) {
    override fun getAnimator(from: View?, to: View?, backward: Boolean): Animator = AnimatorSet()

    override fun startAnimationWhenViewIsReady(view: View, animation: () -> Unit) {
        animation()
    }
}