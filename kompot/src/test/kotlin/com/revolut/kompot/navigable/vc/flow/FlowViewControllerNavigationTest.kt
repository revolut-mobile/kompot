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

package com.revolut.kompot.navigable.vc.flow

import android.os.Bundle
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.components.TestFlowViewController
import com.revolut.kompot.navigable.components.TestFlowViewControllerModel
import com.revolut.kompot.navigable.components.TestStep
import com.revolut.kompot.navigable.components.TestViewController
import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class FlowViewControllerNavigationTest {

    private val flowModel = TestFlowViewControllerModel()
    private val flow = TestFlowViewController(flowModel)

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
    fun `GIVEN initial step WHEN attached THEN render initial step`() {
        flow.onCreate()
        flow.onAttach()

        flow.assertStateRendered(1)
    }

    @Test
    fun `GIVEN initial step WHEN navigate forward THEN go to next step`() {
        flow.onCreate()
        flow.onAttach()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )

        flow.assertStateRendered(2)
    }

    @Test
    fun `GIVEN initial step WHEN navigate backward THEN return to previous step`() {
        flow.onCreate()
        flow.onAttach()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.handleBack()

        flow.assertStateRendered(1)
    }

    @Test
    fun `GIVEN step 2 not added to back stack WHEN navigate backward from step 2 THEN go to step 1`() {
        flow.onCreate()
        flow.onAttach()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flowModel.flowCoordinator.next(
            TestStep(3),
            addCurrentStepToBackStack = false,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.handleBack()

        flow.assertStateRendered(1)
    }

    @Test
    fun `GIVEN flow with step 2 opened WHEN restore from saved state THEN restore step 2`() {
        flow.onCreate()
        flow.onAttach()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )

        val bundle = Bundle()
        flow.saveState(bundle)

        val restoredFlowModel = TestFlowViewControllerModel()
        val restoredFlow = TestFlowViewController(restoredFlowModel)

        restoredFlow.restoreState(bundle)

        restoredFlow.onCreate()
        restoredFlow.onAttach()

        restoredFlow.assertStateRendered(2)
    }

    @Test
    fun `GIVEN initial step WHEN transition to step 2 canceled THEN revert to initial state`() {
        flow.onCreate()
        flow.onAttach()

        val initialController = flowModel.flowCoordinator.getCurrentController()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.mainControllerManager.onTransitionCanceled(
            from = initialController,
            backward = false,
        )

        flow.assertStateRendered(1)
    }

    private fun TestFlowViewController.assertStateRendered(index: Int) {
        val currentFlowController = currentController
        require(currentFlowController is TestViewController)
        assertEquals(index.toString(), currentFlowController.input)
        assertEquals(TestStep(index), model.flowCoordinator.step)
    }
}