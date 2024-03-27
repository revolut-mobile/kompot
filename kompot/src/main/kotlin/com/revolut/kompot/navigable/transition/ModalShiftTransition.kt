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
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.revolut.kompot.navigable.transition.Transition.Companion.DURATION_DEFAULT

internal class ModalShiftTransition(
    private val style: ModalAnimatable.Style,
    private val showImmediately: Boolean = false,
) : Transition {

    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()

    override fun start(from: View?, to: View?, backward: Boolean, transitionListener: TransitionListener) {
        if (!backward) {
            if (showImmediately) {
                startImmediateForwardTransition(to, transitionListener)
            } else {
                startForwardTransition(to, transitionListener)
            }
        } else {
            val animatable = getAnimatable(from)
            if (animatable != null) {
                startBackwardTransition(animatable, transitionListener)
            } else {
                startFallbackBackwardTransition(from, to, transitionListener)
            }
        }
    }

    private fun startForwardTransition(to: View?, transitionListener: TransitionListener) {
        val animatable = requireAnimatable(to)
        transitionListener.onTransitionCreated()
        transitionListener.onTransitionStart()
        animatable.style = style
        animatable.show {
            transitionListener.onTransitionEnd()
            transitionListener.onTransitionFinished()
        }
    }


    private fun startImmediateForwardTransition(to: View?, transitionListener: TransitionListener) {
        val animatable = requireAnimatable(to)
        transitionListener.onTransitionCreated()
        transitionListener.onTransitionStart()
        animatable.style = style
        animatable.showImmediately()
        transitionListener.onTransitionEnd()
        transitionListener.onTransitionFinished()
    }

    private fun startBackwardTransition(animatable: ModalAnimatable, transitionListener: TransitionListener) {
        transitionListener.onTransitionCreated()
        transitionListener.onTransitionStart()
        animatable.hide {
            transitionListener.onTransitionEnd()
            transitionListener.onTransitionFinished()
        }
    }

    private fun startFallbackBackwardTransition(from: View?, to: View?, transitionListener: TransitionListener) {
        //For the cases when animatable class is not used we still should animate back from any view safely
        transitionListener.onTransitionCreated()
        transitionListener.onTransitionStart()
        val animator = ObjectAnimator.ofFloat(from, View.ALPHA, 0f)
        animator.interpolator = interpolator
        animator.duration = DURATION_DEFAULT
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                (to ?: from)!!.post {
                    transitionListener.onTransitionEnd()
                    transitionListener.onTransitionFinished()
                }
            }
        })
        animator.start()
    }

    private fun getAnimatable(view: View?): ModalAnimatable? {
        var currentView = view
        while (currentView != null) {
            val animatable = currentView.tag as? ModalAnimatable
            if (animatable != null) return animatable
            currentView = currentView.parent as? View?
        }
        return null
    }

    private fun requireAnimatable(view: View?): ModalAnimatable = checkNotNull(getAnimatable(view))

    override fun endImmediately() {
        //Do nothing
    }
}