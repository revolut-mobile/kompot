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
import android.os.Bundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.navigable.binder.ModelBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BaseScreenTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val rootFlow: RootFlow<*, *> = mock {
        on { rootDialogDisplayer } doReturn mock()
    }

    @BeforeEach
    fun setUp() {
        Preconditions.mainThreadRequirementEnabled = false
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun reset() {
        Preconditions.mainThreadRequirementEnabled = true
        Dispatchers.resetMain()
    }

    @Test
    fun `propagate state received from model`() {
        val testStates = listOf(TestUIState(1))

        val testUIStateFlow = flowOf(*testStates.toTypedArray())

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        assertEquals(testStates, screen.receivedStates)
    }

    @Test
    fun `receive only latest state available after attach`() {
        val lastState = TestUIState(3)
        val testStates = listOf(TestUIState(1), TestUIState(2), lastState)

        val testUIStateFlow = flowOf(*testStates.toTypedArray())

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        assertEquals(listOf(lastState), screen.receivedStates)
    }

    @Test
    fun `receive all states emitted after on attach`() {
        val testStates = listOf(TestUIState(1), TestUIState(2), TestUIState(3))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testUIStateFlow.tryEmit(testStates[2])

        assertEquals(testStates, screen.receivedStates)
    }

    @Test
    fun `calculate payload for states`() {
        val testStates = listOf(TestUIState(1), TestUIState(2), TestUIState(3))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testUIStateFlow.tryEmit(testStates[2])

        val expectedPayloads = listOf(null, TestPayload(1), TestPayload(2))

        assertEquals(expectedPayloads, screen.receivedPayloads)
    }

    @Test
    fun `no ui state emissions while transition is in progress`() {
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        screen.onTransitionStart(enter = true)

        testUIStateFlow.tryEmit(TestUIState(1))

        assertTrue(screen.receivedStates.isEmpty())
    }

    @Test
    fun `dispatch latest emission after transition ended`() {
        val testStates = listOf(TestUIState(1), TestUIState(2), TestUIState(3))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow)

        screen.onCreate()
        screen.onAttach()

        screen.onTransitionStart(enter = true)

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testUIStateFlow.tryEmit(testStates[2])

        screen.onTransitionEnd(enter = true)

        assertEquals(listOf(testStates[2]), screen.receivedStates)
    }

    @Test
    fun `debounce states when has debounce flow`() {
        val debounceFlow = flow<Unit> { }
        val testStates = listOf(TestUIState(1), TestUIState(2), TestUIState(3))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow, debounceFlow = debounceFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testUIStateFlow.tryEmit(testStates[2])

        assertEquals(listOf(testStates[0]), screen.receivedStates)
    }

    @Test
    fun `receive states after corresponding timeout when debounce activated`() {
        val debounceFlow = flow<Unit> { }
        val testStates = listOf(TestUIState(1), TestUIState(2), TestUIState(3))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow, debounceFlow = debounceFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testDispatcher.advanceTimeBy(300)
        testUIStateFlow.tryEmit(testStates[2])
        testDispatcher.advanceTimeBy(300)

        assertEquals(testStates, screen.receivedStates)
    }

    @Test
    fun `debounce states when debounceFlow emits items`() {
        val debounceFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val testStates = listOf(TestUIState(1), TestUIState(2))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow, debounceFlow = debounceFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testDispatcher.advanceTimeBy(250)
        debounceFlow.tryEmit(Unit)
        testDispatcher.advanceTimeBy(50)

        assertEquals(listOf(testStates[0]), screen.receivedStates)
    }

    @Test
    fun `emit last debounced item`() {
        val debounceFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val testStates = listOf(TestUIState(1), TestUIState(2))
        val testUIStateFlow = MutableSharedFlow<TestUIState>(extraBufferCapacity = 3)

        val screen = TestBaseScreen(testUIStateFlow, debounceFlow = debounceFlow)

        screen.onCreate()
        screen.onAttach()

        testUIStateFlow.tryEmit(testStates[0])
        testUIStateFlow.tryEmit(testStates[1])
        testDispatcher.advanceTimeBy(250)
        debounceFlow.tryEmit(Unit)
        testDispatcher.advanceTimeBy(300)

        assertEquals(listOf(testStates[0], testStates[1]), screen.receivedStates)
    }

    @Test
    fun `invoke screen result on screen model command`() {
        val resultBinder = ModelBinder<IOData.EmptyOutput>()
        var resultInvoked = false
        val screen = TestBaseScreen(resultBinder = resultBinder).apply {
            onScreenResult = {
                resultInvoked = true
            }
        }

        screen.onCreate()
        resultBinder.notify(IOData.EmptyOutput)

        assertTrue(resultInvoked)
    }

    @Test
    fun `invoke root flow back handler on screen model command`() {
        val backCommandsBinder = ModelBinder<Unit>()
        val screen = TestBaseScreen(backCommandsBinder = backCommandsBinder)

        screen.onCreate()
        backCommandsBinder.notify(Unit)

        verify(rootFlow).handleBack()
    }

    inner class TestBaseScreen(
        modelUIStateFlow: Flow<TestUIState> = flowOf(),
        resultBinder: ModelBinder<IOData.EmptyOutput> = ModelBinder(),
        backCommandsBinder: ModelBinder<Unit> = ModelBinder(),
        private val debounceFlow: Flow<Any>? = null
    ) : BaseScreen<TestUIState, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

        val receivedStates = mutableListOf<TestUIState>()
        val receivedPayloads = mutableListOf<TestPayload?>()

        override val layoutId: Int = 0

        override val screenComponent: BaseScreenComponent
            get() = FakeScreenComponent

        override val screenModel: ScreenModel<TestUIState, IOData.EmptyOutput> =
            FakeScreenModel(modelUIStateFlow, resultBinder, backCommandsBinder)

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
            bind(parentControllerManager, parentController = rootFlow)
        }

        override fun debounceStream(): Flow<Any>? = debounceFlow

        override fun bindScreen(uiState: TestUIState, payload: ScreenStates.UIPayload?) {
            receivedStates.add(uiState)
            receivedPayloads.add(payload as TestPayload?)
        }
    }

    class FakeScreenModel(
        private val uiStateFlow: Flow<TestUIState>,
        private val resultBinder: ModelBinder<IOData.EmptyOutput>,
        private val backCommandsBinder: ModelBinder<Unit>
    ) : ControllerModel(), ScreenModel<TestUIState, IOData.EmptyOutput> {
        override fun uiStateStream(): Flow<TestUIState> = uiStateFlow

        override fun saveState(): Bundle = Bundle.EMPTY

        override fun restoreState(state: Bundle) = Unit

        override fun resultsBinder() = resultBinder

        override fun backPressBinder() = backCommandsBinder
    }

    object FakeScreenComponent : BaseScreenComponent {
        override fun getControllerExtensions(): Set<ControllerExtension> = setOf()
    }

    data class TestPayload(val value: Int) : ScreenStates.UIPayload
    data class TestUIState(val value: Int) : ScreenStates.UI {
        override fun calculatePayload(oldState: ScreenStates.UI): ScreenStates.UIPayload? =
            TestPayload((oldState as TestUIState).value)
    }

}