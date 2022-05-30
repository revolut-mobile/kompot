package com.revolut.kompot.navigable.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

internal abstract class AnimatorTransition(
    val duration: Long = DURATION_DEFAULT,
    val withAnimation: Boolean = true
): Transition {
    private var activeAnimator: Animator? = null
    private var activeTransitionListener: TransitionListener? = null
    open val interpolator: Interpolator = AccelerateDecelerateInterpolator()

    abstract fun getAnimator(from: View?, to: View?, backward: Boolean): Animator

    override fun start(
        from: View?,
        to: View?,
        backward: Boolean,
        transitionListener: TransitionListener
    ) {
        if (withAnimation) {
            to?.visibility = View.INVISIBLE
            startAnimationWhenViewIsReady((to ?: from)!!) {
                transitionListener.onTransitionCreated()
                val animator = getAnimator(from, to, backward)
                animate(from, to, animator, transitionListener)
            }
        } else {
            transitionListener.onTransitionCreated()
            transitionListener.onTransitionFinished()
        }
    }

    abstract fun startAnimationWhenViewIsReady(view: View, animation: () -> Unit)

    private fun animate(
        from: View?,
        to: View?,
        animator: Animator,
        transitionListener: TransitionListener
    ) {
        transitionListener.onTransitionStart()

        activeAnimator = animator
        activeTransitionListener = transitionListener
        animator.duration = duration
        animator.interpolator = interpolator
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                activeAnimator = null
                (to ?: from)!!.post { finishActiveTransition() }
            }
        })
        animator.start()
    }

    override fun endImmediately() {
        activeAnimator?.removeAllListeners()
        activeAnimator?.end()
        activeAnimator = null
        finishActiveTransition()
    }

    private fun finishActiveTransition() {
        activeTransitionListener?.onTransitionEnd()
        activeTransitionListener?.onTransitionFinished()
        activeTransitionListener = null
    }

    companion object {
        const val DURATION_DEFAULT = 300L
    }

}