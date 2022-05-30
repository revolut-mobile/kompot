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