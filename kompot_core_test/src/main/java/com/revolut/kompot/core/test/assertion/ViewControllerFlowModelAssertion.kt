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
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.StartPostponedStateRestore
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.flow.FlowViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("CheckResult", "VisibleForTests")
internal class ViewControllerFlowModelAssertion<T, STEP : FlowStep, OUTPUT : IOData.Output> internal constructor(
    private val model: T
) : CommonFlowModelAssertions<STEP, OUTPUT>() where T : FlowViewModel<STEP, OUTPUT>,
                                                    T : ViewControllerModel<OUTPUT> {

    override val eventsDispatcher: EventsDispatcher
        get() = model.eventsDispatcher

    override val hasBackStack: Boolean
        get() = model.flowCoordinator.hasBackStack

    init {
        model.applyTestDependencies(dialogDisplayer = dialogDisplayer)
        model.flowCoordinator.onCreate(ControllerKey(""))
        model.flowCoordinator
            .navigationBinder().asFlow()
            .onEach { command ->
                when (command) {
                    is Next -> model.flowCoordinator.setNextState(command, null)
                    is Quit,
                    is StartPostponedStateRestore -> {
                        commandQueue.add(command)
                    }
                    //Commands for internal communication are not supported
                    else -> {}
                }
            }.launchIn(testScope)

        model.resultStream()
            .onEach {
                commandQueue.add(PostFlowResult(it))
            }.launchIn(testScope)

        model.backStream()
            .onEach {
                model.flowCoordinator.handleBackStack(immediate = true)
                commandQueue.add(Back())
            }.launchIn(testScope)
    }

    override fun getCurrentController(): Controller =
        model.flowCoordinator.getCurrentController()

    override fun getCurrentStep(): STEP = model.flowCoordinator.step
}
