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

interface FlowAssertion<T> {

    val values: List<T>

    val valueCount: Int

    fun assertValueCount(
        expectedCount: Int
    ): FlowAssertion<T>

    fun assertValues(
        vararg expectedValues: T
    ): FlowAssertion<T>

    fun assertValues(
        expectedValues: List<T>
    ): FlowAssertion<T>

    fun assertValueAt(
        index: Int, expectedValue: T
    ): FlowAssertion<T>

    fun assertValueAt(
        index: Int,
        predicate: (actualValue: T) -> Boolean
    ): FlowAssertion<T>

    fun assertNoValues(): FlowAssertion<T>

    fun assertComplete(): FlowAssertion<T>

    fun assertNotCompleted(): FlowAssertion<T>

    fun assertError(
        throwable: Throwable
    ): FlowAssertion<T>

    fun assertNoErrors(): FlowAssertion<T>

    fun <R : Throwable> assertError(
        throwableClass: Class<R>
    ): FlowAssertion<T>
}