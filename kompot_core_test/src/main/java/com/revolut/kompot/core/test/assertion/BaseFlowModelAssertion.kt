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

package com.revolut.kompot.core.test.assertion

import android.annotation.SuppressLint
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.coroutines.Direct
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.StartPostponedStateRestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@SuppressLint("VisibleForTests")
internal class BaseFlowModelAssertion<STEP : FlowStep, OUTPUT : IOData.Output> internal constructor(
    private val flowModel: BaseFlowModel<*, STEP, OUTPUT>
) : CommonFlowModelAssertions<STEP, OUTPUT>() {

    override val eventsDispatcher: EventsDispatcher
        get() = flowModel.eventsDispatcher

    init {
        flowModel.applyTestDependencies(dialogDisplayer = dialogDisplayer)
        flowModel.setInitialState()

        val childFlowModel = FakeFlowModel()

        @OptIn(ExperimentalCoroutinesApi::class)
        testScope.launch(Dispatchers.Direct) {
            flowModel.navigationBinder().asFlow().collect { command ->
                when (command) {
                    is Next -> flowModel.setNextState(
                        command.step,
                        command.animation,
                        command.addCurrentStepToBackStack,
                        childFlowModel
                    )

                    is Back -> {
                        flowModel.handleBackStack(immediate = true)
                        commandQueue.add(command)
                    }

                    is Quit,
                    is PostFlowResult,
                    is StartPostponedStateRestore -> {
                        commandQueue.add(command)
                    }
                    //Commands for internal communication are not supported
                    else -> {}
                }
            }
        }

        flowModel.onCreated()
    }

    override fun getCurrentController(): Controller =
        flowModel.getController(flowModel.step)

    override fun getCurrentStep(): STEP = flowModel.step
}

@SuppressLint("VisibleForTests")
private class FakeFlowModel : BaseFlowModel<FakeState, FakeStep, IOData.EmptyOutput>() {

    override val initialStep: FakeStep = FakeStep
    override val initialState: FakeState = FakeState

    init {
        setInitialState()
    }

    override fun getController(step: FakeStep): Controller {
        throw IllegalStateException()
    }
}

@Parcelize
private object FakeStep : FlowStep

@Parcelize
private object FakeState : FlowState
