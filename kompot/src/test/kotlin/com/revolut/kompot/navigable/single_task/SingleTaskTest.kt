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

package com.revolut.kompot.navigable.single_task

import com.revolut.kompot.advanceTimeImmediatelyBy
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.ControllerModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class SingleTaskTest {

    private val testControllerModel = TestControllerModel()

    @Test
    fun `GIVEN single task flow WHEN concurrent subscription requested THEN do not launch flow concurrently`() =
        dispatchBlockingTest {
            var flowAccessCount = 0

            val testFlow = with(testControllerModel) {
                flow {
                    flowAccessCount++
                    delay(1)
                    emit(Unit)
                }.testSingleTask("")
            }

            testFlow.launchIn(this)
            testFlow.launchIn(this)

            assertEquals(1, flowAccessCount)
        }

    @Test
    fun `GIVEN single task flow WHEN concurrent subscription requested THEN concurrent flow completes`() =
        dispatchBlockingTest {
            val testFlow = with(testControllerModel) {
                flow {
                    delay(1)
                    emit(Unit)
                }.testSingleTask("")
            }

            testFlow.launchIn(this)

            var concurrentFlowCompleted = false
            testFlow
                .onCompletion { cause ->
                    if (cause == null) {
                        concurrentFlowCompleted = true
                    }
                }
                .launchIn(this)

            assertTrue(concurrentFlowCompleted)
        }

    @Test
    fun `GIVEN single task flow WHEN flow completed THEN allow new subscription`() =
        dispatchBlockingTest {
            var flowAccessCount = 0

            val testFlow = with(testControllerModel) {
                flow {
                    flowAccessCount++
                    delay(1)
                    emit(Unit)
                }.testSingleTask("")
            }

            testFlow.launchIn(this)
            advanceTimeImmediatelyBy(1)  //unblock action and complete first subscription

            testFlow.launchIn(this)

            assertEquals(2, flowAccessCount)
        }

    @Test
    fun `GIVEN single task flow AND downstream applied WHEN concurrent subscription THEN do not launch downstream concurrently`() =
        dispatchBlockingTest {
            var downstreamAccessCount = 0

            val testFlow = with(testControllerModel) {
                flow {
                    emit(1)
                }.testSingleTask(
                    taskId = ""
                ).map {
                    downstreamAccessCount++
                    delay(1)
                    2
                }
            }

            testFlow.launchIn(this)
            testFlow.launchIn(this)

            assertEquals(1, downstreamAccessCount)
        }

    @Test
    fun `GIVEN single task flow AND downstream applied WHEN action completed THEN allow new subscription`() =
        dispatchBlockingTest {
            var downstreamAccessCount = 0

            val testFlow = with(testControllerModel) {
                flow {
                    emit(1)
                }.testSingleTask(
                    taskId = ""
                ).map {
                    downstreamAccessCount++
                    delay(1)
                    2
                }
            }

            testFlow.launchIn(this)
            advanceTimeImmediatelyBy(1) //complete first flow and unblock next flow

            testFlow.launchIn(this)

            assertEquals(2, downstreamAccessCount)
        }

    @Test
    fun `GIVEN single task flow WHEN task finished with error THEN propagate error`() =
        dispatchBlockingTest {
            val testFlow = with(testControllerModel) {
                flow<Unit> {
                    throw IllegalStateException()
                }.testSingleTask("")
            }

            assertThrows<IllegalStateException> {
                runBlocking { testFlow.collect() }
            }
        }

    @Test
    fun `GIVEN single task suspend action WHEN concurrent access requested THEN don't launch concurrent action`() =
        dispatchBlockingTest {
            var accessCount = 0
            val action = suspend {
                with(testControllerModel) {
                    testSingleTask("") {
                        accessCount++
                        delay(1)
                    }
                }
            }

            launch { action() }
            launch { action() }

            assertEquals(1, accessCount)
        }

    @Test
    fun `GIVEN single task suspend action WHEN concurrent access requested THEN concurrent action returns null`() =
        dispatchBlockingTest {
            val action = suspend {
                with(testControllerModel) {
                    testSingleTask("") {
                        delay(1)
                    }
                }
            }

            launch { action() }
            assertNull(action())
        }

    @Test
    fun `GIVEN single task suspend action WHEN action completed THEN allow new action`() =
        dispatchBlockingTest {
            var accessCount = 0
            val action = suspend {
                with(testControllerModel) {
                    testSingleTask("") {
                        accessCount++
                        delay(1)
                    }
                }
            }

            launch { action() }
            advanceTimeImmediatelyBy(1) //complete first action and unblock next

            launch { action() }

            assertEquals(2, accessCount)
        }

    @Test
    fun `GIVEN single task suspend action WHEN error in action THEN propagate error`() =
        dispatchBlockingTest {
            val action = suspend {
                with(testControllerModel) {
                    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
                    testSingleTask("") {
                        throw IllegalStateException()
                    }
                }
            }

            assertThrows<IllegalStateException> {
                runBlocking { action() }
            }
        }


    class TestControllerModel : ControllerModel() {

        fun <T> Flow<T>.testSingleTask(taskId: String): Flow<T> =
            singleTask(taskId)

        suspend fun <T> testSingleTask(taskId: String, action: suspend () -> T): T? =
            singleTask(taskId, action)

    }

}