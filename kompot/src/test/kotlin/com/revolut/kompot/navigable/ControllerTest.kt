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

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ControllerTest {


    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `launched flow added to attached scope`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            testFlow.collectTillDetachViewTest()
            testFlow.collectTillDetachViewTest()

            assertEquals(2, attachedScope.childrenCount())
        }
    }

    @Test
    fun `attached scope children canceled after detach`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            testFlow.collectTillDetachViewTest()
            testFlow.collectTillDetachViewTest()

            onDetach()

            assertEquals(0, attachedScope.childrenCount())
        }
    }

    @Test
    fun `attached scope is active after reattach`() {
        val testFlow = MutableStateFlow(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            onDetach()
            onAttach()

            testFlow.collectTillDetachViewTest()

            assertEquals(1, attachedScope.childrenCount())
        }
    }

    @Test
    fun `invoke onEach for flow launched in attached scope`() {
        val testFlow = flowOf(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            var onEachInvoked = false
            testFlow.collectTillDetachViewTest {
                onEachInvoked = true
            }
            assertTrue(onEachInvoked)
        }
    }

    @Test
    fun `handle error for flow launched in attached scope`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }
        with(TestController()) {
            onCreate()
            onAttach()

            var catchedThrowable: Throwable? = null
            testFlow.collectTillDetachViewTest(
                onError = {
                    catchedThrowable = it
                }
            )
            assertEquals(testException, catchedThrowable)
        }
    }

    @Test
    fun `handle error from the collector for flow launched in attached scope`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            var catchedThrowable: Throwable? = null
            testFlow.collectTillDetachViewTest(
                onError = {
                    catchedThrowable = it
                },
                onEach = {
                    throw testException
                }
            )
            assertEquals(testException, catchedThrowable)
        }
    }

    @Test
    fun `invoke onSuccessCompletion for flow launched in attached scope`() {
        val testFlow = flowOf(Unit)
        with(TestController()) {
            onCreate()
            onAttach()

            var onCompletionInvoked = false
            testFlow.collectTillDetachViewTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertTrue(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in attached scope flow occur`() {
        val testException = IllegalStateException()
        val testFlow = flow<Unit> {
            throw testException
        }

        with(TestController()) {
            onCreate()
            onAttach()

            var onCompletionInvoked = false
            testFlow.collectTillDetachViewTest(
                onSuccessCompletion = {
                    onCompletionInvoked = true
                }
            )
            assertFalse(onCompletionInvoked)
        }
    }

    @Test
    fun `do not invoke onSuccessCompletion when error in attached scope flow collector occur`() {
        val testException = IllegalStateException()
        val testFlow = flowOf(Unit)

        with(TestController()) {
            onCreate()
            onAttach()

            var onCompletionInvoked = false
            testFlow.collectTillDetachViewTest(
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
    fun `throw exception when flow collected in attached scope after onDetach`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestController()) {
                onCreate()
                onAttach()
                onDetach()

                flowOf(Unit).collectTillDetachViewTest()
            }
        }
    }

    @Test
    fun `throw exception when flow collected in attached scope after onDestroy`() {
        assertThrows(IllegalStateException::class.java) {
            with(TestController()) {
                onCreate()
                onAttach()
                onDetach()
                onDestroy()

                flowOf(Unit).collectTillDetachViewTest()
            }
        }
    }

    @Test
    fun `controller extensions are initiated on attach`() {
        with(TestController()) {
            onCreate()
            onAttach()

            verify(controllerExtensions.first()).init(attachedScope)
        }
    }

    @Test
    fun `call controller extensions' onParentLifecycleEvent method on any controller's lifecycle method call`() {
        with(TestController()) {
            onCreate()
            onAttach()
            onDetach()
            onDestroy()

            controllerExtensions.first().inOrder {
                verify().onParentLifecycleEvent(Lifecycle.Event.ON_CREATE)
                verify().onParentLifecycleEvent(Lifecycle.Event.ON_RESUME)
                verify().onParentLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                verify().onParentLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        }
    }

    @Test
    fun `GIVEN registered exit transition callback WHEN exit transition ends multiple times THEN trigger callback once`() {
        with(TestController()) {
            var callbackInvocationsCount = 0
            doOnNextExitTransition { callbackInvocationsCount ++ }

            onTransitionStart(enter = false)
            onTransitionEnd(enter = false)

            assertEquals(1, callbackInvocationsCount)

            onTransitionStart(enter = false)
            onTransitionEnd(enter = false)

            assertEquals(1, callbackInvocationsCount)
        }
    }

    private fun CoroutineScope.childrenCount() = coroutineContext.job.children.count()

    inner class TestController : Controller() {

        override val layoutId: Int = 0

        override fun createView(inflater: LayoutInflater): View {
            return view
        }

        override val controllerExtensions: Set<ControllerExtension> = setOf(mock())

        init {
            val mockedActivity = mock<Activity> {
                on { window } doReturn mock()
            }
            view = mock {
                on { context } doReturn mockedActivity
            }
            val parentControllerManager: ControllerManager = mock {
                on { controllersCache } doReturn mock()
            }
            bind(parentControllerManager, parentController = mock())
        }

        fun <T> Flow<T>.collectTillDetachViewTest(
            onError: suspend (Throwable) -> Unit = { },
            onSuccessCompletion: suspend () -> Unit = {},
            onEach: suspend (T) -> Unit = {}
        ): Job = collectTillDetachView(onError, onSuccessCompletion, onEach)

    }

}