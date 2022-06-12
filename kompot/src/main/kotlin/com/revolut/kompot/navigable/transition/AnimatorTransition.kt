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