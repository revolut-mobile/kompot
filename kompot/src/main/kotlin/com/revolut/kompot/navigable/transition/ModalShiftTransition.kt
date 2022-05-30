package com.revolut.kompot.navigable.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

internal class ModalShiftTransition(
    private val style: ModalAnimatable.Style
) : Transition {

    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()

    private fun getAnimatable(view: View?): ModalAnimatable? {
        var currentView = view
        while (currentView != null) {
            val animatable = currentView.tag as? ModalAnimatable
            if (animatable != null) return animatable
            currentView = currentView.parent as? View?
        }
        return null
    }

    override fun start(from: View?, to: View?, backward: Boolean, transitionListener: TransitionListener) {
        transitionListener.onTransitionCreated()
        transitionListener.onTransitionStart()
        if (!backward) {
            getAnimatable(to)?.let { animatable ->
                animatable.style = style
                animatable.show {
                    transitionListener.onTransitionEnd()
                    transitionListener.onTransitionFinished()
                }
            }
        } else {
            val animatable = getAnimatable(from)
            if (animatable != null) {
                animatable.let { animatable ->
                    animatable.hide {
                        transitionListener.onTransitionEnd()
                        transitionListener.onTransitionFinished()
                    }
                }
            } else {
                //For the cases when animatable class is not used we still should animate back from any view safely
                transitionListener.onTransitionCreated()
                transitionListener.onTransitionStart()
                val animator = ObjectAnimator.ofFloat(from, View.ALPHA, 0f)
                animator.interpolator = interpolator
                animator.duration = AnimatorTransition.DURATION_DEFAULT
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        (to ?: from)!!.post {
                            transitionListener.onTransitionEnd()
                            transitionListener.onTransitionFinished()
                        }
                    }
                })
                animator.start()
            }

        }
    }

    override fun endImmediately() {
        //Do nothing
    }
}