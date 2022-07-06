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

package com.revolut.kompot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
internal fun dispatchBlockingTest(
    block: suspend KompotTestScope.() -> Unit,
) = runTest(context = UnconfinedTestDispatcher()) {
    block(KompotTestScope(wrappedTestScope = this))
    coroutineContext.cancelChildren()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class KompotTestScope(
    private val wrappedTestScope: TestScope
) : CoroutineScope by wrappedTestScope {

    val testScheduler: TestCoroutineScheduler = wrappedTestScope.testScheduler
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun KompotTestScope.advanceTimeImmediatelyBy(delayTimeMillis: Long) {
    testScheduler.advanceTimeBy(delayTimeMillis)
    testScheduler.runCurrent()
}