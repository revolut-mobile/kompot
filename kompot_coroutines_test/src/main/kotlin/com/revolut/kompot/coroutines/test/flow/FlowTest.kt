package com.revolut.kompot.coroutines.test.flow

import com.revolut.kompot.coroutines.test.KompotTestScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Launches a new coroutine to collect values/events from the flow asynchronously.
 * Useful for testing hot flows, when events are emitted after testIn() invoked (e.g model states).
 *
 * Should be used in conjunction with dispatchBlockingTest{} that provides a necessary KompotTestScope
 *
 * @param kompotTestScope Scope which collects values/events from the flow under test.
 *
 * @return FlowAssertion that provides set of methods to assert state of the flow
 */
fun <T : Any> Flow<T>.testIn(
    kompotTestScope: KompotTestScope
): FlowAssertion<T> {
    val testFlow = this
    val testProcessor = TestProcessor<T>()
    kompotTestScope.launch(
        start = CoroutineStart.UNDISPATCHED,
        context = Dispatchers.Unconfined
    ) {
        testFlow.processWith(testProcessor)
    }
    return FlowAssertionImpl(testProcessor)
}

private suspend fun <T : Any> Flow<T>.processWith(testProcessor: TestProcessor<T>) {
    try {
        collect { item ->
            testProcessor.processEvent(TestEvent.Item(item))
        }
        testProcessor.processEvent(TestEvent.Completion())
    } catch (_: CancellationException) {

    } catch (t: Throwable) {
        testProcessor.processEvent(TestEvent.Error(t))
    }
}

internal class TestProcessor<T> {

    private var _testState: TestState<T> = TestState.Running(emptyList())
    val testState: TestState<T> get() = _testState

    fun processEvent(event: TestEvent<T>) {
        val currentState = _testState
        check(currentState is TestState.Running) {
            "Cannot process events of the finished flow"
        }
        _testState = when (event) {
            is TestEvent.Item -> {
                currentState.copy(
                    items = currentState.items + event.item
                )
            }
            is TestEvent.Completion -> {
                TestState.Completed(items = currentState.items)
            }
            is TestEvent.Error -> {
                TestState.Error(
                    items = currentState.items,
                    throwable = event.throwable
                )
            }
        }
    }

}

internal sealed class TestState<T> {
    abstract val items: List<T>

    data class Running<T>(override val items: List<T>) : TestState<T>()

    data class Error<T>(
        override val items: List<T>,
        val throwable: Throwable
    ) : TestState<T>()

    data class Completed<T>(
        override val items: List<T>,
    ) : TestState<T>()
}

internal sealed class TestEvent<T> {
    data class Item<T>(val item: T) : TestEvent<T>()
    data class Error<T>(val throwable: Throwable) : TestEvent<T>()
    class Completion<T> : TestEvent<T>()
}