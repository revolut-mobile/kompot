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
import com.revolut.kompot.navigable.components.TestFlow
import com.revolut.kompot.navigable.components.TestFlowModel
import com.revolut.kompot.navigable.components.TestState
import com.revolut.kompot.navigable.components.TestStep
import com.revolut.kompot.navigable.cache.DefaultControllersCache
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
        flowModel.changeState(2)
        flowModel.next(TestStep(2), true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel()
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        assertEquals(TestStep(2), restoredFlowModel.step)
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
        flowModel.next(TestStep(2), true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel()
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        restoredFlow.handleBack()

        //First entry from the back stack should be restored
        assertEquals(TestStep(1), restoredFlowModel.step)
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
        flow.onAttach()

        //change internal state of the flow and the nested flow
        //by moving each of them to the next step
        nestedFlowModel.changeState(newValue = 2)
        nestedFlowModel.next(TestStep(2), true)
        flowModel.changeState(newValue = 2)
        flowModel.next(TestStep(2), true)

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
        assertEquals(TestStep(2), restoredNestedFlowModel.step)
        assertEquals(TestState(2), restoredNestedFlowModel.stateWrapper.state)
    }

    @Test
    fun `should not restore state from bundle if postpone from saved state enabled`() {
        val flowModel = TestFlowModel(postponeSavedStateRestore = true)
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep(2), true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        //check that Step and State are the same as initial
        assertEquals(TestStep(1), restoredFlowModel.step)
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
        flowModel.next(TestStep(2), true)

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
        assertEquals(TestStep(1), restoredFlowModel.step)
        assertEquals(TestState(1), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `should postpone state restore`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.changeState(2)
        flowModel.next(TestStep(2), true)

        val bundle = Bundle()

        flow.saveState(bundle)

        val restoredFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        assertTrue(restoredFlowModel.startPostponedSavedStateRestore())

        assertEquals(TestStep(2), restoredFlowModel.step)
        assertEquals(TestState(2), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `GIVEN canceled parent flow transition WHEN restore from saved state THEN restore nested flow latest state`() {
        val nestedFlowModel = TestFlowModel()
        val nestedFlow = TestFlow(nestedFlowModel)

        val parentFlowModel = TestFlowModel(firstStepController = nestedFlow)
        val parentFlow = TestFlow(parentFlowModel)

        val rootFlowModel = TestFlowModel(firstStepController = parentFlow)
        val rootFlow = TestFlow(rootFlowModel)

        rootFlow.onCreate()
        rootFlow.onAttach()

        parentFlowModel.next(TestStep(2), true)
        //trigger transition cancellation
        parentFlow.getOrCreateChildControllerManager(
            controllerContainer = parentFlow.mainControllerContainer,
            id = parentFlow.mainControllerContainer.containerId,
        ).onTransitionCanceled(
            from = nestedFlow,
            backward = false
        )

        //modify nested flow
        nestedFlowModel.next(TestStep(2), true)

        val bundle = Bundle()

        rootFlow.saveState(bundle)

        //instantiate new flows to simulate app state restore
        val restoredNestedFlowModel = TestFlowModel()
        val restoredNestedFlow = TestFlow(restoredNestedFlowModel)

        val restoredParentFlowModel = TestFlowModel(firstStepController = restoredNestedFlow)
        val restoredParentFlow = TestFlow(restoredParentFlowModel)

        val restoredRootFlowModel = TestFlowModel(firstStepController = restoredParentFlow)
        val restoredRootFlow = TestFlow(restoredRootFlowModel)

        restoredParentFlow.bind(parentControllerManager, restoredRootFlow)
        restoredNestedFlow.bind(parentControllerManager, restoredParentFlow)

        //push bundle to the flow
        restoredRootFlow.restoreState(RestorationPolicy.FromBundle(bundle))

        restoredRootFlow.onCreate()
        restoredRootFlow.onAttach()

        assertEquals(TestStep(1), restoredParentFlowModel.step)
        assertEquals(TestStep(2), restoredNestedFlowModel.step) //nested flow restored successfully
    }

    @Test
    fun `GIVEN flow with pending saved state WHEN try save flow state THEN don't save state`() {
        //create bundle with flow's saved state
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)
        flow.onCreate()
        flow.onAttach()
        val bundle = Bundle()
        flow.saveState(bundle)

        //simulate flow that postpones saved state restoration, but doesn't trigger startPostponedSavedStateRestore()
        val pendingRestoreFlowModel = TestFlowModel(postponeSavedStateRestore = true)
        val pendingRestoreFlow = TestFlow(pendingRestoreFlowModel)

        pendingRestoreFlow.restoreState(RestorationPolicy.FromBundle(bundle))
        pendingRestoreFlow.onCreate()
        pendingRestoreFlow.onAttach()
        pendingRestoreFlowModel.changeState(2)
        pendingRestoreFlowModel.next(TestStep(2), true)

        val bundle2 = Bundle()
        //try to save state
        //Should do nothing, because we can't save state of the flow that has a pending saved state and doesn't trigger startPostponedSavedStateRestore()
        pendingRestoreFlow.saveState(bundle2)

        //try to restore flow from from the bundle
        val restoredFlowModel = TestFlowModel()
        val restoredFlow = TestFlow(restoredFlowModel)

        restoredFlow.restoreState(RestorationPolicy.FromBundle(bundle2))
        restoredFlow.onCreate()
        restoredFlow.onAttach()

        //Flow has initial values, there was nothing to restore
        assertEquals(TestStep(1), restoredFlowModel.step)
        assertEquals(TestState(1), restoredFlowModel.stateWrapper.state)
    }

    @Test
    fun `GIVEN restored flow with backstack WHEN handleBack, cancel transition and handleBack again THEN controller is reused`() {
        // Save flow into bundle
        var flowModel = TestFlowModel().apply {
            randomiseControllerKey = true
        }
        var flow = TestFlow(flowModel, DefaultControllersCache(10))
        flow.apply {
            onCreate()
            onAttach()
        }
        flowModel.apply {
            next(TestStep(2), true)
            next(TestStep(3), true)
        }
        val bundle = Bundle()
        flow.saveState(bundle)


        // Recreate and restore
        flowModel = TestFlowModel().apply {
            randomiseControllerKey = true
        }
        flow = TestFlow(flowModel, DefaultControllersCache(10))
        flow.apply {
            restoreState(bundle)
            onCreate()
            onAttach()
        }
        // Handle back and cancel
        flow.handleBack()
        assertEquals(TestStep(2), flowModel.step)
        val controller2 = flowModel.getController()
        flow.getOrCreateChildControllerManager(
            controllerContainer = flow.mainControllerContainer,
            id = flow.mainControllerContainer.containerId,
        ).onTransitionCanceled(
            from = controller2,
            backward = true
        )

        // Handle back again, assert cached controller is used
        flow.handleBack()
        assertEquals(flowModel.getController(), controller2)
    }

}