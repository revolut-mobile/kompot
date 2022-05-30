package com.revolut.kompot.navigable.transition

import com.revolut.kompot.navigable.TransitionAnimation
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
            arrayOf(TransitionAnimation.SLIDE_RIGHT_TO_LEFT, SlideTransition::class),
            arrayOf(TransitionAnimation.SLIDE_LEFT_TO_RIGHT, SlideTransition::class),
            arrayOf(TransitionAnimation.FADE, FadeTransition::class),
        )
    }

}