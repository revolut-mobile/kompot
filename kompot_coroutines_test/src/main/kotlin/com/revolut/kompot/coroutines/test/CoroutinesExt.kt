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

package com.revolut.kompot.coroutines.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
fun dispatchBlockingTest(
    context: CoroutineContext = TestDispatchersHolder.unconfinedTestDispatcher,
    block: suspend KompotTestScope.() -> Unit,
) = runTest(context = context) {
    block(KompotTestScopeImpl(wrappedTestScope = this))
    coroutineContext.cancelChildren()
}

@OptIn(ExperimentalCoroutinesApi::class)
interface KompotTestScope : CoroutineScope {

    val testScheduler: TestCoroutineScheduler
}

@OptIn(ExperimentalCoroutinesApi::class)
private class KompotTestScopeImpl(
    private val wrappedTestScope: TestScope
) : KompotTestScope, CoroutineScope by wrappedTestScope {

    override val testScheduler: TestCoroutineScheduler = wrappedTestScope.testScheduler
}

@OptIn(ExperimentalCoroutinesApi::class)
fun KompotTestScope.advanceTimeImmediatelyBy(delayTimeMillis: Long) {
    advanceTimeBy(delayTimeMillis)
    runCurrent()
}

@ExperimentalCoroutinesApi
val KompotTestScope.currentTime: Long
    get() = testScheduler.currentTime

@ExperimentalCoroutinesApi
fun KompotTestScope.advanceUntilIdle(): Unit = testScheduler.advanceUntilIdle()

@ExperimentalCoroutinesApi
fun KompotTestScope.runCurrent(): Unit = testScheduler.runCurrent()

@ExperimentalCoroutinesApi
fun KompotTestScope.advanceTimeBy(delayTimeMillis: Long): Unit = testScheduler.advanceTimeBy(delayTimeMillis)

@ExperimentalCoroutinesApi
@ExperimentalTime
val KompotTestScope.testTimeSource: TimeSource
    get() = testScheduler.timeSource