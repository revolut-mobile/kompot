package com.revolut.kompot.coroutines.test

import com.revolut.kompot.coroutines.test.flow.testIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import org.junit.jupiter.api.Test

internal class FlowAssertionDemo {

    @Test
    fun `hot flow`() = dispatchBlockingTest {
        val hotFlow = MutableSharedFlow<Int>()
        val hotFlowTest = hotFlow.testIn(this)

        hotFlow.emit(1)
        hotFlow.emit(2)
        hotFlow.emit(3)

        hotFlowTest
            .assertValueCount(3)
            .assertValues(1, 2, 3)
    }

    @Test
    fun `cold flow`() = dispatchBlockingTest {
        val flow = flow {
            emit(1)
            emit(2)
            emit(3)
        }

        flow
            .testIn(this)
            .assertValueCount(3)
            .assertValues(1, 2, 3)
            .assertComplete()
    }

}