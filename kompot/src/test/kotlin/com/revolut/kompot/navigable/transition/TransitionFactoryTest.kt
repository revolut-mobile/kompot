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

import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.navigable.ModalTransitionAnimation
import com.revolut.kompot.navigable.TransitionAnimation
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

internal class TransitionFactoryTest {

    private val factory = TransitionFactory()

    @ParameterizedTest
    @MethodSource("transitionCreationTestArgs")
    fun `create correct transition for given animation type`(
        transitionAnimation: TransitionAnimation,
        expectedTransitionClass: KClass<Transition>
    ) {
        val actualTransition = factory.createTransition(transitionAnimation)

        assertEquals(expectedTransitionClass, actualTransition::class)
    }

    @Suppress("unused")
    companion object {

        @JvmStatic
        fun transitionCreationTestArgs() = arrayOf(
            arrayOf(TransitionAnimation.NONE, ImmediateTransition::class),
            arrayOf(TransitionAnimation.SLIDE_LEFT_TO_RIGHT, SlideTransition::class),
            arrayOf(TransitionAnimation.SLIDE_RIGHT_TO_LEFT, SlideTransition::class),
            arrayOf(TransitionAnimation.FADE, FadeTransition::class),
            arrayOf(ModalTransitionAnimation.ModalPopup(), ModalShiftTransition::class),
            arrayOf(ModalTransitionAnimation.ModalFullscreenSlideFromBottom(), ModalShiftTransition::class),
            arrayOf(ModalTransitionAnimation.BottomDialog(), ModalShiftTransition::class),
            arrayOf(TestCustomTransitionAnimation, CustomTransition::class),
            arrayOf(
                ModalTransitionAnimation.ModalFullscreenFade(
                    showImmediately = false,
                    style = ModalDestination.Style.FULLSCREEN_IMMEDIATE
                ),
                ModalShiftTransition::class
            ),
            arrayOf(
                ModalTransitionAnimation.ModalFullscreenFade(
                    showImmediately = true,
                    style = ModalDestination.Style.FULLSCREEN_FADE
                ),
                ModalShiftTransition::class
            ),
        )
    }

    @Parcelize
    private object TestCustomTransitionAnimation : TransitionAnimation.Custom {
        override val providerId: Int get() = 0
    }

}