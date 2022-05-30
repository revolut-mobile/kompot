package com.revolut.kompot.navigable

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.LifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ControllerModelTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `launched flow added to shown scope`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            testFlow.collectTillHideTest()
            testFlow.collectTillHideTest()

            assertEquals(2, shownScope.childrenCount())
        }
    }

    @Test
    fun `shown scope children canceled after hidden`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            testFlow.collectTillHideTest()
            testFlow.collectTillHideTest()

            onLifecycleEvent(LifecycleEvent.HIDDEN)

            assertEquals(0, shownScope.childrenCount())
        }
    }

    @Test
    fun `shown scope is active after reattach`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            onLifecycleEvent(LifecycleEvent.HIDDEN)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            testFlow.collectTillHideTest()

            assertEquals(1, shownScope.childrenCount())
        }
    }

    @Test
    fun `invoke onEach for flow launched in shown scope`() {
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var onEachInvoked = false
            testFlow.collectTillHideTest {
                onEachInvoked = true
            }
            assertTrue(onEachInvoked)
        }
    }

    @Test
    fun `handle error for flow launched in shown scope`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var catchedThrowable: Throwable? = null
            testFlow.collectTillHideTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            )
            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `handle error from the collector for flow launched in shown scope`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var catchedThrowable: Throwable? = null
            testFlow.collectTillHideTest(
                handleError = {
                    catchedThrowable = it
                    false
                },
                onEach = {
                    throw testException
                }
            )
            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `invoke onSuccessCompletion for flow launched in shown scope`() {
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var onCompletionInvoked = false
            testFlow.collectTillHideTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertTrue(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in shown scope flow occur`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var onCompletionInvoked = false
            testFlow.collectTillHideTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertFalse(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in shown scope flow collector occur`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var onCompletionInvoked = false
            testFlow.collectTillHideTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                },
                onEach = {
                    throw testException
                }
            )
            assertFalse(onCompletionInvoked)
        }
    }

    @Test
    fun `throw exception when flow collected in shown scope after onHidden`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)

                flowOf(Unit).collectTillHideTest()
            }
        }
    }

    @Test
    fun `throw exception when flow collected in shown scope after onFinished`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)
                onLifecycleEvent(LifecycleEvent.FINISHED)

                flowOf(Unit).collectTillHideTest()
            }
        }
    }

    @Test
    fun `launched flow added to created scope`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            testFlow.collectTillFinishTest()

            assertTrue(createdScope.coroutineContext.isActive)
            assertFalse(createdScope.coroutineContext.job.isCancelled)

            val childrenCount = createdScope.coroutineContext.job.children.count()
            assertEquals(1, childrenCount)
        }
    }

    @Test
    fun `invoke onEach for flow launched in created scope`() {
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var onEachInvoked = false
            testFlow.collectTillFinishTest {
                onEachInvoked = true
            }
            assertTrue(onEachInvoked)
        }
    }

    @Test
    fun `handle error for flow launched in created scope`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var catchedThrowable: Throwable? = null
            testFlow.collectTillFinishTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            )
            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `handle error from the collector for flow launched in created scope`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var catchedThrowable: Throwable? = null
            testFlow.collectTillFinishTest(
                handleError = {
                    catchedThrowable = it
                    false
                },
                onEach = {
                    throw testException
                }
            )
            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `invoke onSuccessCompletion for flow launched in created scope`() {
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var onCompletionInvoked = false
            testFlow.collectTillFinishTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertTrue(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in created scope flow occur`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var onCompletionInvoked = false
            testFlow.collectTillFinishTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertFalse(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in created scope flow collector occur`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var onCompletionInvoked = false
            testFlow.collectTillFinishTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                },
                onEach = {
                    throw testException
                }
            )
            assertFalse(onCompletionInvoked)
        }
    }

    @Test
    fun `throw exception when flow collected in created scope after onFinished`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)
                onLifecycleEvent(LifecycleEvent.FINISHED)

                flowOf(Unit).collectTillFinishTest { }
            }
        }
    }

    @Test
    fun `launch coroutine in shown scope`() {
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            tillHideTest {
                MutableStateFlow(Unit).collect() //simulate busy coroutine
            }

            assertTrue(shownScope.coroutineContext.isActive)
            assertFalse(shownScope.coroutineContext.job.isCancelled)

            val childrenCount = shownScope.coroutineContext.job.children.count()
            assertEquals(1, childrenCount)
        }
    }

    @Test
    fun `handle error for coroutine launched in shown scope`() {
        val testException = IllegalStateException()
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var catchedThrowable: Throwable? = null
            tillHideTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            ) {
                throw testException
            }

            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `throw exception when coroutine launched in shown scope after onHidden`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)

                tillHideTest { }
            }
        }
    }

    @Test
    fun `throw exception when coroutine launched in shown scope after onFinished`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)
                onLifecycleEvent(LifecycleEvent.FINISHED)

                tillHideTest { }
            }
        }
    }

    @Test
    fun `launch coroutine in created scope`() {
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            tillFinishTest {
                MutableStateFlow(Unit).collect() //simulate busy coroutine
            }

            assertEquals(1, createdScope.childrenCount())
        }
    }

    @Test
    fun `created scope children canceled after finished`() {
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            tillFinishTest {
                MutableStateFlow(Unit).collect() //simulate busy coroutine
            }

            onLifecycleEvent(LifecycleEvent.FINISHED)

            assertEquals(0, createdScope.childrenCount())
        }
    }

    @Test
    fun `handle error for coroutine launched in created scope`() {
        val testException = IllegalStateException()
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var catchedThrowable: Throwable? = null
            tillFinishTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            ) {
                throw testException
            }

            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `throw exception when coroutine launched in created scope after onFinished`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestControllerModel()) {
                onLifecycleEvent(LifecycleEvent.CREATED)
                onLifecycleEvent(LifecycleEvent.SHOWN)
                onLifecycleEvent(LifecycleEvent.HIDDEN)
                onLifecycleEvent(LifecycleEvent.FINISHED)

                tillFinishTest { }
            }
        }
    }

    @Test
    fun `wrap suspend block with loading`() {
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            tillFinishTest { withLoadingTest {} }

            inOrder(dialogDisplayer).apply {
                verify(dialogDisplayer).showLoadingDialog(any())
                verify(dialogDisplayer).hideLoadingDialog()
            }
        }
    }

    @Test
    fun `handle error for coroutine launched in created scope - async`() {
        val testException = IllegalStateException()
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)

            var catchedThrowable: Throwable? = null
            tillFinishTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            ) {
                val exceptionDeferred = async { throw testException }
                exceptionDeferred.await()
            }

            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    @Test
    fun `handle error for coroutine launched in shown scope - async`() {
        val testException = IllegalStateException()
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.SHOWN)

            var catchedThrowable: Throwable? = null
            tillHideTest(
                handleError = {
                    catchedThrowable = it
                    false
                }
            ) {
                val exceptionDeferred = async { throw testException }
                exceptionDeferred.await()
            }

            assertEquals(testException, catchedThrowable)
            verify(eventsDispatcher).handleEvent(ErrorEvent(testException))
        }
    }

    private fun CoroutineScope.childrenCount() = coroutineContext.job.children.count()

    inner class TestControllerModel : ControllerModel() {

        init {
            injectDependencies(
                dialogDisplayer = mock(),
                eventsDispatcher = mock(),
                controllersCache = mock(),
                mainDispatcher = testDispatcher
            )
        }

        fun <T> Flow<T>.collectTillHideTest(
            handleError: suspend (Throwable) -> Boolean = { false },
            onSuccessCompletion: suspend () -> Unit = {},
            onEach: suspend (T) -> Unit = {}
        ): Job = collectTillHide(handleError, onSuccessCompletion, onEach)

        fun <T> Flow<T>.collectTillFinishTest(
            handleError: suspend (Throwable) -> Boolean = { false },
            onSuccessCompletion: suspend () -> Unit = {},
            onEach: suspend (T) -> Unit = {}
        ): Job = collectTillFinish(handleError, onSuccessCompletion, onEach)

        fun <T> tillHideTest(
            handleError: (Throwable) -> Boolean = { false },
            block: suspend CoroutineScope.() -> T
        ) = tillHide(handleError, block)

        fun <T> tillFinishTest(
            handleError: (Throwable) -> Boolean = { false },
            block: suspend CoroutineScope.() -> T
        ) = tillFinish(handleError, block)

        suspend fun <T> withLoadingTest(block: suspend () -> T): T =
            withLoading(block)

    }
}