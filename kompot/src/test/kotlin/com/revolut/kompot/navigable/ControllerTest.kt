package com.revolut.kompot.navigable

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private fun CoroutineScope.childrenCount() = coroutineContext.job.children.count()

    inner class TestController : Controller() {

        override val layoutId: Int = 0

        override fun createView(inflater: LayoutInflater): View {
            return view
        }

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