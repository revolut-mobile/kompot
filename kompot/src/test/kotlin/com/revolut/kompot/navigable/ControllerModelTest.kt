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

package com.revolut.kompot.navigable

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.revolut.kompot.common.ControllerDescriptor
import com.revolut.kompot.common.ControllerHolder
import com.revolut.kompot.common.ControllerRequest
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.ErrorInterceptedEventResult
import com.revolut.kompot.common.ErrorInterceptionEvent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.common.NavigationRequest
import com.revolut.kompot.common.NavigationRequestEvent
import com.revolut.kompot.common.NavigationRequestResult
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.utils.showModal
import com.revolut.kompot.navigable.vc.ViewController
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ControllerModelTest {

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
    fun `GIVEN error intercepted WHEN collect THEN do not call handle error`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestControllerModel()) {
            whenever(this.eventsDispatcher.handleEvent(any<ErrorInterceptionEvent>())) doReturn ErrorInterceptedEventResult
            onLifecycleEvent(LifecycleEvent.CREATED)

            var handleErrorCalled = false
            testFlow.collectTillFinishTest(
                handleError = {
                    handleErrorCalled = true
                    false
                }
            )
            assertFalse(handleErrorCalled)
            verify(eventsDispatcher).handleEvent(ErrorInterceptionEvent(testException))
            verify(eventsDispatcher, never()).handleEvent(any<ErrorEvent>())
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

    @Test
    fun `extensions are initiated when controller model injectDependencies is called`() {
        with(TestControllerModel()) {
            verify(extensions.first()).init(this)
        }
    }

    @Test
    fun `extension's onParentLifecycleScope is called whenever parent's lifecycle is changed`() {
        with(TestControllerModel()) {
            onLifecycleEvent(LifecycleEvent.SHOWN)
            verify(extensions.first()).onParentLifecycleEvent(LifecycleEvent.SHOWN)
            onLifecycleEvent(LifecycleEvent.HIDDEN)
            verify(extensions.first()).onParentLifecycleEvent(LifecycleEvent.HIDDEN)
            onLifecycleEvent(LifecycleEvent.CREATED)
            verify(extensions.first()).onParentLifecycleEvent(LifecycleEvent.CREATED)
            onLifecycleEvent(LifecycleEvent.FINISHED)
            verify(extensions.first()).onParentLifecycleEvent(LifecycleEvent.FINISHED)
        }
    }

    @Test
    fun `GIVEN controller bound to descriptor WHEN show as modal THEN dispatch modal command`() {
        val descriptor = object : ControllerDescriptor<IOData.EmptyOutput> {}
        val vc = mock<ViewController<IOData.EmptyOutput>>()

        val controllerRequestResult = ControllerHolder(vc)

        with(TestControllerModel()) {
            whenever(eventsDispatcher.handleEvent(ControllerRequest(descriptor)))
                .thenReturn(controllerRequestResult)

            descriptor.getController().showModalTest()

            verify(eventsDispatcher).handleEvent(
                NavigationEvent(ModalDestination.CallbackController(vc))
            )
        }
    }

    @Test
    fun `GIVEN nav command bound to nav request WHEN navigate via request THEN dispatch nav command`() = dispatchBlockingTest {
        val navRequest = object : NavigationRequest {}
        val navCommand = object : NavigationDestination {}

        with(TestControllerModel()) {
            whenever(eventsDispatcher.handleEvent(NavigationRequestEvent(navRequest)))
                .thenReturn(NavigationRequestResult { navCommand })

            navRequest.navigate()

            verify(eventsDispatcher).handleEvent(NavigationEvent(navCommand))
        }
    }

    @Test
    fun `GIVEN nav request failure WHEN navigate via request THEN propagate exception`() {
        val navRequest = object : NavigationRequest {}

        with(TestControllerModel()) {
            whenever(eventsDispatcher.handleEvent(NavigationRequestEvent(navRequest)))
                .thenThrow(IllegalStateException())

            assertThrows<IllegalStateException> {
                runBlocking { navRequest.navigate() }
            }
        }
    }

    private fun CoroutineScope.childrenCount() = coroutineContext.job.children.count()

    inner class TestControllerModel : ControllerModel() {

        val extensions: Set<ControllerModelExtension> = setOf(mock())

        init {
            injectDependencies(
                dialogDisplayer = mock(),
                eventsDispatcher = mock(),
                controllersCache = mock(),
                mainDispatcher = UnconfinedTestDispatcher(),
                controllerModelExtensions = extensions,
            )
        }

        fun <T : IOData.Output> ViewController<T>.showModalTest(
            style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN,
            onResult: ((T) -> Unit)? = null
        ) = showModal(eventsDispatcher, style, onResult)

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