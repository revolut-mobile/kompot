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
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.RestorationPolicy
import com.revolut.kompot.navigable.flow.StartPostponedStateRestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class BaseFlowModelTest {

    @Test
    fun `update navigation stream when next step requested`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        val expectedCommand = Next<TestStep, IOData.EmptyOutput>(
            step = TestStep(1),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE
        )

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(1).toList()
            assertEquals(listOf(expectedCommand), actual)
        }

        flowModel.next(TestStep(1), addCurrentStepToBackStack = true, animation = TransitionAnimation.NONE)
    }

    @Test
    fun `update navigation stream on back command`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(1).toList()
            assertTrue(actual[0] is Back)
        }

        flowModel.back()
    }

    @Test
    fun `update navigation stream on quit command`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(1).toList()
            assertTrue(actual[0] is Quit)
        }

        flowModel.quitFlow()
    }

    @Test
    fun `update navigation stream when result posted`() = dispatchBlockingTest {
        val flowModel = createTestFlowModel()

        val expectedCommands = listOf<PostFlowResult<TestStep, IOData.EmptyOutput>>(
            PostFlowResult(IOData.EmptyOutput), PostFlowResult(IOData.EmptyOutput)
        )

        launch {
            val actual = flowModel.navigationBinder().asFlow().take(2).toList()
            assertEquals(expectedCommands, actual)
        }

        flowModel.postFlowResult(IOData.EmptyOutput)
        flowModel.postFlowResult(IOData.EmptyOutput)
    }

    @Test
    fun `should go to the next state`() {
        with(createTestFlowModel()) {
            testNext(stateValue = 2)

            assertFlowModelState(2)
        }
    }

    @Test
    fun `should navigate to previous step`() {
        with(createTestFlowModel()) {
            testNext(stateValue = 2)
            testBack()

            assertFlowModelState(1)
        }
    }

    @Test
    fun `should restore to the selected step`() {
        with(createTestFlowModel()) {
            testNext(stateValue = 2)
            testNext(stateValue = 3)

            restoreToStep(1)

            assertFlowModelState(1)
        }
    }

    @ParameterizedTest
    @MethodSource("restorationArgs")
    fun `should decide if restoration needed based on restoration policy and postponed state`(
        restorationPolicy: RestorationPolicy,
        postponeStateRestore: Boolean,
        restorationNeeded: Boolean
    ) {
        val flowModel = TestFlowModel(postponeStateRestore = postponeStateRestore).apply {
            restoreState(restorationPolicy)
            onLifecycleEvent(LifecycleEvent.CREATED)
        }

        assertEquals(restorationNeeded, flowModel.restorationNeeded)
    }

    @Test
    fun `restoration is not needed if restoreState not invoked`() {
        val flowModel = TestFlowModel().apply {
            onLifecycleEvent(LifecycleEvent.CREATED)
        }
        assertFalse(flowModel.restorationNeeded)
    }

    @Test
    fun `start postponed state restoration if restore was previously postponed`() = dispatchBlockingTest {
        val flowModel = TestFlowModel(postponeStateRestore = true).apply {
            restoreState(RestorationPolicy.FromBundle(Bundle()))
            onLifecycleEvent(LifecycleEvent.CREATED)
        }

        launch {
            val actualCommand = flowModel.navigationBinder().asFlow().first()
            assertTrue(actualCommand is StartPostponedStateRestore)
        }

        assertTrue(flowModel.startPostponedSavedStateRestore())
    }

    @Test
    fun `don't start postponed state restoration twice`() = dispatchBlockingTest {
        val flowModel = TestFlowModel(postponeStateRestore = true).apply {
            restoreState(RestorationPolicy.FromBundle(Bundle()))
            setInitialState()
        }

        flowModel.startPostponedSavedStateRestore()
        assertFalse(flowModel.startPostponedSavedStateRestore())
    }

    private fun TestFlowModel.assertFlowModelState(stateValue: Int) {
        assertEquals(TestStep(stateValue), step)
        assertEquals(TestState(stateValue), curState)
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
                val restorationNeeded = true

                arrayOf(restorationPolicy, postponeStateRestore, restorationNeeded)
            },
            Arguments {
                val restorationPolicy = RestorationPolicy.FromBundle(Bundle())
                val postponeStateRestore = true
                val restorationNeeded = false

                arrayOf(restorationPolicy, postponeStateRestore, restorationNeeded)
            },
            Arguments {
                val restorationPolicy = RestorationPolicy.FromParent(mock())
                val postponeStateRestore = true
                val restorationNeeded = false

                arrayOf(restorationPolicy, postponeStateRestore, restorationNeeded)
            }
        )
    }

    class TestFlowModel
        (private val postponeStateRestore: Boolean = false
    ) : BaseFlowModel<TestState, TestStep, IOData.EmptyOutput>() {

        override val initialStep: TestStep = TestStep(1)
        override val initialState: TestState = TestState(1)

        private val childFlowModel: TestFlowModel by lazy(LazyThreadSafetyMode.NONE) {
            TestFlowModel().apply { this.setInitialState() }
        }

        val curState: TestState get() = currentState

        override fun getController(step: TestStep): Controller = mock()

        fun testNext(stateValue: Int) {
            val step = TestStep(stateValue)
            val addCurrentToBackStack = true
            next(step, addCurrentToBackStack) {
                TestState(stateValue)
            }
            setNextState(step, TransitionAnimation.NONE, addCurrentToBackStack, childFlowModel)
        }

        fun testBack() {
            restorePreviousState()
        }

        fun restoreToStep(stateValue: Int) {
            restoreToStep(
                StepRestorationCriteria.RestoreByStep(
                    condition = {
                        (it as TestStep).value == stateValue
                    },
                    removeCurrent = true
                )
            )
            testBack()
        }

        override fun postponeSavedStateRestore(): Boolean = postponeStateRestore

    }

    @Parcelize
    data class TestState(val value: Int) : FlowState

    @Parcelize
    data class TestStep(val value: Int) : FlowStep

}