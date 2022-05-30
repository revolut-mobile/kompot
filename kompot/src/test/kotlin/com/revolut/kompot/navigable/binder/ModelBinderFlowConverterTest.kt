package com.revolut.kompot.navigable.binder

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.dispatchBlockingTest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModelBinderFlowConverterTest {

    @Test
    fun `should pass binder events to observing flow`() = dispatchBlockingTest {
        val binder = ModelBinder<Int>()

        val actualValues = mutableListOf<Int>()
        launch {
            binder.asFlow().toList(actualValues)
        }

        binder.notify(1)
        binder.notify(2)

        val expectedValues = listOf(1, 2)

        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `should release binding when observing flow is canceled`() = dispatchBlockingTest {
        val mockedBinding = mock<Binding>()
        val binder = mock<ModelBinder<Int>> {
            on { bind(any()) } doReturn mockedBinding
        }

        binder.asFlow().launchIn(this).cancel()

        verify(mockedBinding).clear()
    }

}