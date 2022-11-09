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

package com.revolut.kompot.navigable.saved_state

import android.os.Bundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TestFlow
import com.revolut.kompot.navigable.TestFlowModel
import com.revolut.kompot.navigable.TestState
import com.revolut.kompot.navigable.TestStep
import com.revolut.kompot.navigable.flow.RestorationPolicy
import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class FlowSavedStateTest {

    private val parentControllerManager: ControllerManager = mock {
        on { controllersCache } doReturn mock()
    }

    @Before
    fun setUp() {
        Preconditions.mainThreadRequirementEnabled = false
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Preconditions.mainThreadRequirementEnabled = true
        Dispatchers.resetMain()
    }

    @Test
    fun `should restore state from bundle`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel()
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        assertEquals(TestStep.Step2, restoredFlowModel.step)
        assertEquals(TestState(2), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `should restore back stack state from bundle`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()

        //mutate current step state
        flowModel.changeState(-1)
        //navigate to the next step. state will change accordingly
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel()
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        restoredFlow.handleBack()

        //First entry from the back stack should be restored
        assertEquals(TestStep.Step1, restoredFlowModel.step)
        assertEquals(TestState(-1), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `should restore back stack with nested flow from bundle`() {
        val nestedFlowModel = TestFlowModel()
        val nestedFlow = TestFlow(nestedFlowModel)

        //instantiate test flow that has nested flow as the first step
        val flowModel = TestFlowModel(firstStepController = nestedFlow)
        val flow = TestFlow(flowModel)

        //run lifecycle events for the flows
        flow.onCreate()
        nestedFlow.onCreate()

        flow.onAttach()
        nestedFlow.onAttach()

        //change internal state of the flow and the nested flow
        //by moving each of them to the next step
        nestedFlowModel.next(TestStep.Step2, true)
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        //instantiate new flows to mock app state restore
        val restoredNestedFlowModel = TestFlowModel()
        val restoredNestedFlow = TestFlow(restoredNestedFlowModel)

        val restoredFlowModel = TestFlowModel(firstStepController = restoredNestedFlow)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredNestedFlow.bind(parentControllerManager, restoredFlow)

        //push bundle to the flow
        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        //restored flow should be created with the Step2
        //handleBack() should navigate flow to the previous state
        restoredFlow.handleBack()

        restoredNestedFlow.onCreate()
        restoredNestedFlow.onAttach()

        //check that nested flow restored its step
        assertEquals(TestStep.Step2, restoredNestedFlowModel.step)
        assertEquals(TestState(2), restoredNestedFlowModel.stateWrapper.state)
    }

    @Test
    fun `should not restore state from bundle if postpone from saved state enabled`() {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true)
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        //check that Step and State are the same as initial
        assertEquals(TestStep.Step1, restoredFlowModel.step)
        assertEquals(TestState(1), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `should not restore back stack state from bundle if postpone from saved state enabled`() {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true)
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()

        //mutate state of the flow
        flowModel.changeState(newValue = 11)
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        //instantiate new flow to mock app state restore
        val restoredFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        restoredFlow.handleBack()

        //check that Step and State are the same as initial
        assertEquals(TestStep.Step1, restoredFlowModel.step)
        assertEquals(TestState(1), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `should postpone state restore`() {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true)
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        assertTrue(restoredFlowModel.startPostponedSavedStateRestore())

        assertEquals(TestStep.Step2, restoredFlowModel.step)
        assertEquals(TestState(2), restoredFlowModel.stateWrapper.state)
    }

}