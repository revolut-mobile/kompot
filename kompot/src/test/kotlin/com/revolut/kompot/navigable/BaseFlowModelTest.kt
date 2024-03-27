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

import android.os.Bundle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.PushControllerCommand
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.RestorationPolicy
import com.revolut.kompot.navigable.flow.RestorationState
import com.revolut.kompot.navigable.flow.StartPostponedStateRestore
import com.revolut.kompot.navigable.components.TestController
import com.revolut.kompot.navigable.components.TestFlowModel
import com.revolut.kompot.navigable.components.TestFlowStep
import com.revolut.kompot.navigable.components.TestStep
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class BaseFlowModelTest {

    @Test
    fun `should push initial controller after created`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        val expectedCommand = PushControllerCommand<TestFlowStep, IOData.EmptyOutput>(
            controller = TestController("1"),
            fromSavedState = false,
            animation = TransitionAnimation.NONE,
            backward = false,
            executeImmediately = true,
        )

        launch {
            val actualCommand = flowModel.navigationBinder().asFlow().first()
            assertEquals(expectedCommand, actualCommand)
        }

        flowModel.onLifecycleEvent(LifecycleEvent.CREATED)
    }

    @Test
    fun `update navigation stream when next step requested`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        val expectedCommand = Next<TestStep, IOData.EmptyOutput>(
            step = TestStep(1),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE
        )

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(2).last()
            assertEquals(expectedCommand, actual)
        }

        flowModel.next(TestStep(1), addCurrentStepToBackStack = true, animation = TransitionAnimation.NONE)
    }

    @Test
    fun `update navigation stream on back command`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(2).last()
            assertTrue(actual is Back)
        }

        flowModel.back()
    }

    @Test
    fun `update navigation stream on quit command`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(2).last()
            assertTrue(actual is Quit)
        }

        flowModel.quitFlow()
    }

    @Test
    fun `update navigation stream when result posted`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        val expectedFlowResultCommand = PostFlowResult<TestFlowStep, IOData.EmptyOutput>(IOData.EmptyOutput)

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(3).toList()
            assertEquals(expectedFlowResultCommand, actual[1])
            assertEquals(expectedFlowResultCommand, actual[2])
        }

        flowModel.postFlowResult(IOData.EmptyOutput)
        flowModel.postFlowResult(IOData.EmptyOutput)
    }

    @Test
    fun `should go to the next state`() {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)

            assertFlowState(2)
        }
    }

    @Test
    fun `should clear cache if addCurrentStepToBackStack set to false`() {
        with(createTestFlowModel()) {
            activate()
            simulateNext(stateValue = 2, addCurrentToBackStack = false)

            verify(controllersCache).removeController(
                controllerKey = ControllerKey("1"),
                finish = false
            )
        }
    }

    @Test
    fun `should dispatch push controller command when handles back stack`() = dispatchBlockingTest {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)

            val expectedCommand = PushControllerCommand<TestFlowStep, IOData.EmptyOutput>(
                controller = TestController("1"),
                fromSavedState = true,
                animation = TransitionAnimation.NONE,
                backward = true,
                executeImmediately = true,
            )
            launch {
                val actual = navigationBinder().asFlow().take(2).last()
                assertEquals(expectedCommand, actual)
            }

            assertTrue(handleBackStack(immediate = true))
            assertFlowState(1)
        }
    }

    @Test
    fun `should revert to latest state if forward transition was canceled`() = dispatchBlockingTest {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)

            val expectedCommand = PushControllerCommand<TestFlowStep, IOData.EmptyOutput>(
                controller = TestController("1"),
                fromSavedState = true,
                animation = TransitionAnimation.NONE,
                backward = true,
                executeImmediately = true,
            )
            launch {
                val actual = navigationBinder().asFlow().take(2).last()
                assertEquals(expectedCommand, actual)
            }

            onTransitionCanceled(backward = false)

            assertFlowState(1)
        }
    }

    @Test
    fun `should revert state dismiss if backward transition was canceled`() = dispatchBlockingTest {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)
            handleBackStack(immediate = true)

            assertFlowState(stateValue = 1)

            onTransitionCanceled(backward = true)

            assertFlowState(stateValue = 2)

            //check that back stack is still correct
            handleBackStack(immediate = true)
            assertFlowState(stateValue = 1)
        }
    }

    @Test
    fun `don't perform back navigation if back stack is empty`() = dispatchBlockingTest {
        with(createTestFlowModel()) {
            val actualCommands = mutableListOf<FlowNavigationCommand<TestStep, IOData.EmptyOutput>>()
            val commandsCollection = launch {
                navigationBinder().asFlow()
                    .onEach { actualCommands.add(it) }
                    .launchIn(this)
            }
            assertFalse(handleBackStack(immediate = true))

            assertTrue(actualCommands.size == 1) //has only initial command
            assertFalse((actualCommands.first() as PushControllerCommand).backward)

            commandsCollection.cancel()
        }
    }

    @Test
    fun `should update state after navigation to the previous step`() {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)
            handleBackStack(immediate = true)

            assertFlowState(1)
        }
    }

    @Test
    fun `should restore to the selected step`() {
        with(createTestFlowModel()) {
            simulateNext(stateValue = 2)
            simulateNext(stateValue = 3)

            restoreToStep(1)

            assertFlowState(1)
        }
    }

    @ParameterizedTest
    @MethodSource("restorationArgs")
    fun `should decide if restoration needed based on restoration policy and postponed state`(
        restorationPolicy: RestorationPolicy,
        postponeStateRestore: Boolean,
        restorationState: RestorationState,
    ) {
        val flowModel = TestFlowModel(postponeSavedStateRestore = postponeStateRestore).apply {
            setInitialState()
            restoreState(restorationPolicy)
            onLifecycleEvent(LifecycleEvent.CREATED)
        }

        assertEquals(restorationState, flowModel.currentRestorationState)
    }

    @Test
    fun `GIVEN restoration required WHEN performCreate THEN push screen with required saved state restoration`() = dispatchBlockingTest {
        val expectedCommand = PushControllerCommand<TestFlowStep, IOData.EmptyOutput>(
            controller = TestController("1"),
            fromSavedState = true,
            animation = TransitionAnimation.NONE,
            backward = false,
            executeImmediately = true,
        )

        TestFlowModel(postponeSavedStateRestore = false).apply {
            launch {
                val actualCommand = navigationBinder().asFlow().first()
                assertEquals(expectedCommand, actualCommand)
            }
            setInitialState()
            restoreState(RestorationPolicy.FromBundle(Bundle()))
            onLifecycleEvent(LifecycleEvent.CREATED)
        }
    }

    @Test
    fun `restoration is not needed if restoreState not invoked`() {
        val flowModel = TestFlowModel().apply {
            onLifecycleEvent(LifecycleEvent.CREATED)
        }
        assertNull(flowModel.currentRestorationState)
    }

    @Test
    fun `start postponed state restoration if restore was previously postponed`() = dispatchBlockingTest {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true).apply {
            restoreState(RestorationPolicy.FromBundle(Bundle()))
            onLifecycleEvent(LifecycleEvent.CREATED)
        }

        launch {
            val actualCommand = flowModel.navigationBinder().asFlow().take(2).last()
            assertTrue(actualCommand is StartPostponedStateRestore)
        }

        assertTrue(flowModel.startPostponedSavedStateRestore())
    }

    @Test
    fun `don't start postponed state restoration twice`() = dispatchBlockingTest {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true).apply {
            restoreState(RestorationPolicy.FromBundle(Bundle()))
            setInitialState()
        }

        flowModel.startPostponedSavedStateRestore()
        assertFalse(flowModel.startPostponedSavedStateRestore())
    }

    private fun createTestFlowModel() = TestFlowModel().apply {
        onLifecycleEvent(LifecycleEvent.CREATED)
    }

    @Suppress("unused")
    companion object {

        @JvmStatic
        fun restorationArgs() = arrayOf(
            Arguments {
                val restorationPolicy = RestorationPolicy.FromBundle(Bundle())
                val postponeStateRestore = false

                arrayOf(restorationPolicy, postponeStateRestore, RestorationState.REQUIRED)
            },
            Arguments {
                val restorationPolicy = RestorationPolicy.FromBundle(Bundle())
                val postponeStateRestore = true

                arrayOf(restorationPolicy, postponeStateRestore, RestorationState.POSTPONED)
            },
            Arguments {
                val restorationPolicy = RestorationPolicy.FromParent(mock())
                val postponeStateRestore = true

                arrayOf(restorationPolicy, postponeStateRestore, RestorationState.POSTPONED)
            }
        )
    }
}