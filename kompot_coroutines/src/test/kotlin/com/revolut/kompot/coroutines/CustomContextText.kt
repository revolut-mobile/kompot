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

package com.revolut.kompot.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.coroutines.ContinuationInterceptor

@OptIn(ExperimentalCoroutinesApi::class)
internal class CustomContextText {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val defaultTestScope = TestScope(CustomContextWrapper(testDispatcher))

    @Test
    fun `should replace dispatchers in test body`() = withReplacedDispatchers {
        defaultTestScope.runTest {
            withContext(Dispatchers.IO) {
                assertTestThread()
            }
        }
    }

    @Test
    fun `should replace dispatchers in the nested coroutines`() = withReplacedDispatchers {
        defaultTestScope.runTest {
            launch(Dispatchers.IO) {
                assertTestThread()
                withContext(Dispatchers.Default) {
                    assertTestThread()
                }
                launch(Dispatchers.Default) {
                    assertTestThread()
                    withContext(Dispatchers.IO) {
                        assertTestThread()
                    }
                    withContext(Dispatchers.Unconfined) {
                        assertTestThread()
                    }
                }
            }
        }
    }

    @Test
    fun `should advance time in the nested coroutines with replaced dispatcher`() = withReplacedDispatchers {
        defaultTestScope.runTest {
            var jobCompleted = false
            launch {
                withContext(Dispatchers.Default) {
                    delay(100_000)
                }
                jobCompleted = true
            }
            advanceUntilIdle()
            assertTrue(jobCompleted)
        }
    }

    @ParameterizedTest
    @MethodSource("originalDispatchersTest")
    fun `should keep original dispatcher if it's not replaced`(
        dispatcher: CoroutineDispatcher,
        expectedDispatcherName: String
    ) = defaultTestScope.runTest {
        withContext(dispatcher) {
            assertEquals(expectedDispatcherName, coroutineContext[ContinuationInterceptor].toString())
        }
    }

    @Test
    fun `should replace dispatchers when wraps a combined context`() = withReplacedDispatchers {
        val testScope = TestScope(CustomContextWrapper(SupervisorJob() + testDispatcher))

        testScope.runTest {
            withContext(Dispatchers.IO) {
                assertTestThread()
            }
        }
    }

    @Test
    fun `should replace dispatchers when another context added to the right`() = withReplacedDispatchers {
        val testScope = TestScope(CustomContextWrapper(testDispatcher) + SupervisorJob())

        testScope.runTest {
            withContext(Dispatchers.IO) {
                assertTestThread()
            }
        }
    }

    private fun assertTestThread() {
        assertTrue(Thread.currentThread().name.contains("Test"))
    }

    private fun withReplacedDispatchers(block: () -> Unit) {
        AppDispatchers.dispatcherOverride = { testDispatcher }
        block()
        AppDispatchers.dispatcherOverride = { it() }
    }

    companion object {
        @JvmStatic
        fun originalDispatchersTest() = arrayOf(
            arrayOf(Dispatchers.Default, "Dispatchers.Default"),
            arrayOf(Dispatchers.IO, "Dispatchers.IO"),
            arrayOf(Dispatchers.Unconfined, "Dispatchers.Unconfined"),
        )
    }
}