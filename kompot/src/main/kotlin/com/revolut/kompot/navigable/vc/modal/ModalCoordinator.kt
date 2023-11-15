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

package com.revolut.kompot.navigable.vc.modal

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.handleNavigationEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.flow.FlowStep

class ModalCoordinator<Step : FlowStep, Out : IOData.Output>(
    private val hostModel: ControllerModel,
    private val controllersFactory: ModalCoordinator<Step, Out>.(Step) -> Controller
) {

    internal fun openModal(step: Step) {
        val controller = controllersFactory(step)
        val style = ModalDestination.Style.POPUP
        hostModel.eventsDispatcher.handleNavigationEvent(
            destination = ModalDestination.CallbackController(
                controller = controller,
                style = style,
            )
        )
    }
}

@Suppress("FunctionName")
fun <S : FlowStep, Out : IOData.Output, T> T.ModalCoordinator(
    controllersFactory: ModalCoordinator<S, Out>.(S) -> Controller,
): ModalCoordinator<S, Out> where T : ModalHostViewModel<S, Out>,
                                  T : ControllerModel =
    ModalCoordinator(
        hostModel = this,
        controllersFactory = controllersFactory
    )