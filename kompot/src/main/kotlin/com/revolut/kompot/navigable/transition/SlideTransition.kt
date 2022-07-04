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