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

import org.opentest4j.AssertionFailedError

internal class FlowAssertionImpl<T : Any>(
    val testProcessor: TestProcessor<T>
) : FlowAssertion<T> {

    private val testState: TestState<T>
        get() = testProcessor.testState

    override val values: List<T>
        get() = testState.items

    override val valueCount: Int
        get() = values.size

    override fun assertValueCount(expectedCount: Int): FlowAssertion<T> {
        if (values.size != expectedCount) {
            fail(
                message = "Value count do not match",
                expected = expectedCount,
                actual = values.size
            )
        }
        return this
    }

    override fun assertValues(vararg expectedValues: T): FlowAssertion<T> {
        val expected = expectedValues.toList()
        if (values != expected) {
            fail(expected = expected, actual = values)
        }
        return this
    }

    override fun assertValues(expectedValues: List<T>): FlowAssertion<T> {
        if (values != expectedValues) {
            fail(expected = expectedValues, actual = values)
        }
        return this
    }

    override fun assertValueAt(index: Int, expectedValue: T): FlowAssertion<T> {
        val actualValue = values[index]
        if (actualValue != expectedValue) {
            fail(expected = expectedValue, actual = actualValue)
        }
        return this
    }

    override fun assertValueAt(index: Int, predicate: (actualValue: T) -> Boolean): FlowAssertion<T> {
        if (!predicate(values[index])) {
            fail(message = "Value not present")
        }
        return this
    }

    override fun assertNoValues(): FlowAssertion<T> {
        if (values.isNotEmpty()) {
            fail(message = "Expected no values but got $values")
        }
        return this
    }

    override fun assertComplete(): FlowAssertion<T> {
        if (testState !is TestState.Completed) {
            fail("Flow not completed")
        }
        return this
    }

    override fun assertError(throwable: Throwable): FlowAssertion<T> {
        val actualThrowable = (testState as? TestState.Error<T>)?.throwable
        if (actualThrowable != throwable) {
            fail(expected = throwable, actual = actualThrowable)
        }
        return this
    }

    override fun <R : Throwable> assertError(throwableClass: Class<R>): FlowAssertion<T> {
        val actualThrowable = (testState as? TestState.Error<T>)?.throwable
        if (!throwableClass.isInstance(actualThrowable)) {
            fail(expected = throwableClass, actual = actualThrowable)
        }
        return this
    }

    private fun fail(message: String) {
        throw AssertionFailedError(message)
    }

    private fun fail(message: String = "Values do not match", expected: Any, actual: Any?) {
        throw AssertionFailedError(message, expected, actual)
    }

}

fun <T : Any> FlowAssertion<T>.assertLatestValue(predicate: (actualValue: T) -> Boolean) =
    assertValueAt(valueCount - 1, predicate)

fun <T : Any> FlowAssertion<T>.assertLastValue(expected: T) =
    assertValueAt(valueCount - 1, expected)
