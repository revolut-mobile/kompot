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

package com.revolut.kompot.navigable.flow

import android.os.Build
import com.revolut.kompot.navigable.components.TestFlow
import com.revolut.kompot.navigable.components.TestFlowModel
import com.revolut.kompot.navigable.components.TestStep
import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
internal class BaseFlowTest {

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
    fun `WHEN transition canceled THEN revert model state`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()

        flowModel.next(TestStep(2), addCurrentStepToBackStack = true)
        flowModel.changeState(2)
        flowModel.assertFlowState(2)

        flow.getOrCreateChildControllerManager(
            controllerContainer = flow.mainControllerContainer,
            id = flow.mainControllerContainer.containerId,
        ).also {
            it.onTransitionCanceled(
                from = flow,
                backward = false,
            )
        }

        flowModel.assertFlowState(1)
    }
}