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

package com.revolut.kompot.navigable

import com.revolut.kompot.common.ModalDestination
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class TransitionAnimationTest {

    @ParameterizedTest
    @MethodSource("modalStyleToTransitionAnimationMapping")
    fun `GIVEN modal destination style WHEN map to modal transition THEN return corresponding transition animation`(
        style: ModalDestination.Style,
        showImmediately: Boolean,
        modalTransitionAnimation: ModalTransitionAnimation,
    ) {
        val actualTransitionAnimation = style.toModalTransitionAnimation(showImmediately)
        assertEquals(modalTransitionAnimation, actualTransitionAnimation)
    }

    @ParameterizedTest
    @MethodSource("modalStyleToTransitionAnimationMapping")
    fun `GIVEN modal transition WHEN map to modal style THEN return corresponding style`(
        style: ModalDestination.Style,
        showImmediately: Boolean,
        modalTransitionAnimation: ModalTransitionAnimation,
    ) {
        val actualStyle = modalTransitionAnimation.extractModalStyle()
        assertEquals(style, actualStyle)
    }

    @Test
    fun `GIVEN non-modal transition WHEN map to modal style THEN return null`() {
        assertNull(TransitionAnimation.SLIDE_LEFT_TO_RIGHT.extractModalStyle())
    }

    companion object {

        @JvmStatic
        fun modalStyleToTransitionAnimationMapping() = arrayOf(
            run {
                val style = ModalDestination.Style.POPUP
                val showImmediately = true
                val modalTransitionAnimation = ModalTransitionAnimation.ModalPopup(true)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.POPUP
                val showImmediately = false
                val modalTransitionAnimation = ModalTransitionAnimation.ModalPopup(false)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_FADE
                val showImmediately = true
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenFade(
                    showImmediately = true,
                    style = style,
                )
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_FADE
                val showImmediately = false
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenFade(
                    showImmediately = false,
                    style = style,
                )
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_SLIDE_FROM_BOTTOM
                val showImmediately = true
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenSlideFromBottom(true)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_SLIDE_FROM_BOTTOM
                val showImmediately = false
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenSlideFromBottom(false)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.BOTTOM_DIALOG
                val showImmediately = true
                val modalTransitionAnimation = ModalTransitionAnimation.BottomDialog(true)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.BOTTOM_DIALOG
                val showImmediately = false
                val modalTransitionAnimation = ModalTransitionAnimation.BottomDialog(false)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_IMMEDIATE
                val showImmediately = true
                //using immediate fade transition to resolve immediate style
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenFade(true, style)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
            run {
                val style = ModalDestination.Style.FULLSCREEN_IMMEDIATE
                val showImmediately = false
                //showImmediately flag overwritten
                val modalTransitionAnimation = ModalTransitionAnimation.ModalFullscreenFade(true, style)
                Arguments.of(style, showImmediately, modalTransitionAnimation)
            },
        )
    }
}