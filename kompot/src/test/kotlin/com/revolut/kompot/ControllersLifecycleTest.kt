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

package com.revolut.kompot

import android.os.Build
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.components.TestFlowStep
import com.revolut.kompot.navigable.components.TestFlowViewController
import com.revolut.kompot.navigable.components.TestFlowViewControllerModel
import com.revolut.kompot.navigable.components.TestRootFlow
import com.revolut.kompot.navigable.components.TestStep
import com.revolut.kompot.navigable.flow.ReusableFlowStep
import com.revolut.kompot.utils.StubMainThreadRule
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
internal class ControllersLifecycleTest {

    @[Rule JvmField]
    val stubMainThreadRule = StubMainThreadRule()

    private val flowModel = TestFlowViewControllerModel()
    private val flow = TestFlowViewController(flowModel)
    private val rootFlow = TestRootFlow(flow)

    @Test
    fun `GIVEN backstack WHEN root controller manager destroyed THEN destroy all controllers, clear cache`() {
        rootFlow.show()
        flow.onAttach()
        rootFlow.onAttach()

        flowModel.flowCoordinator.next(
            TestStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )

        val allControllers = buildList<Controller> {
            this += rootFlow
            this += flow
            this += flowModel.initialisedControllers
        }
        assertEquals(4, allControllers.size)
        allControllers.forEach { controller ->
            assertFalse(controller.destroyed)
            assertTrue(rootFlow.controllerManager.controllersCache.isControllerCached(controller.key))
        }

        rootFlow.controllerManager.onDestroy()

        allControllers.forEach { controller ->
            assertTrue(controller.destroyed)
            assertFalse(rootFlow.controllerManager.controllersCache.isControllerCached(controller.key))
        }
    }

    @Test
    fun `GIVEN flow with multiple reusable steps initialised WHEN root controller manager destroyed THEN destroy all controllers, clear cache`() {
        val flowModel = TestFlowViewControllerModel(ReusableStep(1))
        val flow = TestFlowViewController(flowModel)
        val rootFlow = TestRootFlow(flow)

        rootFlow.show()
        flow.onAttach()
        rootFlow.onAttach()

        flowModel.flowCoordinator.next(
            ReusableStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.assertStateRendered(stateValue = 2)
        flowModel.flowCoordinator.next(
            ReusableStep(1),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.assertStateRendered(stateValue = 1)
        flowModel.flowCoordinator.next(
            ReusableStep(2),
            addCurrentStepToBackStack = true,
            animation = TransitionAnimation.NONE,
            executeImmediately = false,
        )
        flow.assertStateRendered(stateValue = 2)

        val allControllers = buildList<Controller> {
            this += rootFlow
            this += flow
            this += flowModel.initialisedControllers
        }
        assertEquals(4, allControllers.size) //reusable controllers initialised only once
        allControllers.forEach { controller ->
            assertFalse(controller.destroyed)
            assertTrue(rootFlow.controllerManager.controllersCache.isControllerCached(controller.key))
        }

        rootFlow.controllerManager.onDestroy()

        allControllers.forEach { controller ->
            assertTrue(controller.destroyed)
            assertFalse(rootFlow.controllerManager.controllersCache.isControllerCached(controller.key))
        }
    }

    @Parcelize
    private data class ReusableStep(override val value: Int) : TestFlowStep, ReusableFlowStep {
        override val key: String get() = value.toString()
    }
}