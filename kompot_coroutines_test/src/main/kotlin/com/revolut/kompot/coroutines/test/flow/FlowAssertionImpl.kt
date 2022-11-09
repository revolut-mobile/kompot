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

package com.revolut.kompot.coroutines.test.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

internal class FlowAssertionImpl<T>(
    private val testProcessor: TestProcessor<T>
) : FlowAssertion<T> {

    private val testState: TestState<T>
        get() = testProcessor.testState

    override val values: List<T>
        get() = testState.items

    override val valueCount: Int
        get() = values.size

    override fun assertValueCount(expectedCount: Int): FlowAssertion<T> {
        assertEquals(expectedCount, values.size, "Value count do not match")
        return this
    }

    override fun assertValues(vararg expectedValues: T): FlowAssertion<T> {
        val expected = expectedValues.toList()
        assertEquals(expected, values)
        return this
    }

    override fun assertValues(expectedValues: List<T>): FlowAssertion<T> {
        assertEquals(expectedValues, values)
        return this
    }

    override fun assertValueAt(index: Int, expectedValue: T): FlowAssertion<T> {
        val actualValue = values[index]
        assertEquals(expectedValue, actualValue)
        return this
    }

    override fun assertValueAt(index: Int, predicate: (actualValue: T) -> Boolean): FlowAssertion<T> {
        assertTrue({ predicate(values[index]) }, "Value not present")
        return this
    }

    override fun assertNoValues(): FlowAssertion<T> {
        assertTrue({ values.isEmpty() }, "Expected no values but got $values")
        return this
    }

    override fun assertComplete(): FlowAssertion<T> {
        assertTrue({ testState is TestState.Completed }, "Flow not completed")
        return this
    }

    override fun assertError(throwable: Throwable): FlowAssertion<T> {
        val actualThrowable = (testState as? TestState.Error<T>)?.throwable
        assertEquals(throwable, actualThrowable)
        return this
    }

    override fun <R : Throwable> assertError(throwableClass: Class<R>): FlowAssertion<T> {
        val actualThrowable = (testState as? TestState.Error<T>)?.throwable
        assertTrue({ throwableClass.isInstance(actualThrowable) }, "expected $throwableClass but was ${actualThrowable?.javaClass}")
        return this
    }
}

fun <T> FlowAssertion<T>.assertLatestValue(predicate: (actualValue: T) -> Boolean) =
    assertValueAt(valueCount - 1, predicate)

fun <T> FlowAssertion<T>.assertLastValue(expected: T) =
    assertValueAt(valueCount - 1, expected)
