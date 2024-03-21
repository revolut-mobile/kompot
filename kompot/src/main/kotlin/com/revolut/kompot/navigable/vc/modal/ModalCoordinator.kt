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

import android.os.Bundle
import android.os.Parcelable
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.handleNavigationEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.SavedStateOwner
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize

class ModalCoordinator<Step : FlowStep, Out : IOData.Output>(
    private val hostModel: ControllerModel,
    private val controllersFactory: ModalCoordinator<Step, Out>.(Step) -> Controller
) {

    private var pendingBundle: Bundle? = null
    private val modalsStack = mutableListOf<ModalStackEntry<Step>>()

    internal fun openModal(step: Step, style: ModalDestination.Style) {
        val controller = controllersFactory(step)
        hostModel.eventsDispatcher.handleNavigationEvent(
            destination = ModalDestination.CallbackController(
                controller = controller,
                style = style,
            )
        )
        registerModalStackEntry(controller, step)
    }

    fun performCreate() {
        pendingBundle?.let(::restoreModalsStack)
        pendingBundle = null
    }

    fun saveState(outBundle: Bundle) {
        if (modalsStack.isEmpty()) return

        val stackSnapshot = modalsStack.map { entry ->
            val controllerState = Bundle()
            (entry.controller as? SavedStateOwner)?.saveState(controllerState)
            ModalStackEntrySnapshot(
                index = entry.index,
                step = entry.step,
                controllerState = controllerState
            )
        }
        outBundle.putParcelableArrayList(MODALS_STACK_ARG, ArrayList(stackSnapshot))
    }

    fun restoreState(bundle: Bundle) {
        pendingBundle = bundle
    }

    private fun restoreModalsStack(bundle: Bundle) {
        val modalsStack: List<ModalStackEntrySnapshot<Step>> = bundle.getParcelableArrayList(MODALS_STACK_ARG) ?: return

        modalsStack.forEach { entry ->
            val controller = controllersFactory(entry.step)
            val controllerState = entry.controllerState
            if (controller is SavedStateOwner) {
                controller.doOnCreate { controller.restoreState(controllerState) }
            }
            val style = ModalDestination.Style.POPUP
            hostModel.eventsDispatcher.handleEvent(
                event = ModalRestorationRequest(
                    index = entry.index,
                    modalController = controller,
                    style = style,
                )
            )
            registerModalStackEntry(controller, entry.step)
        }
    }

    private fun registerModalStackEntry(controller: Controller, step: Step) {
        val modalStackEntry = ModalStackEntry(
            index = modalsIndex++,
            step = step,
            controller = controller,
        )
        controller.doOnCreate {
            modalsStack.add(modalStackEntry)
        }
        controller.doOnDestroy { modalsStack.remove(modalStackEntry) }
    }

    private class ModalStackEntry<Step : FlowStep>(
        val index: Int,
        val step: Step,
        val controller: Controller,
    )

    @Parcelize
    private class ModalStackEntrySnapshot<Step : FlowStep>(
        val index: Int,
        val step: Step,
        val controllerState: Bundle,
    ) : Parcelable

    companion object {
        private const val MODALS_STACK_ARG = "MODALS_STACK_ARG"
        private var modalsIndex = 0
    }
}

fun <S : FlowStep, Out : IOData.Output, T> T.ModalCoordinator(
    controllersFactory: ModalCoordinator<S, Out>.(S) -> Controller,
): ModalCoordinator<S, Out> where T : ModalHostViewModel<S, Out>,
                                  T : ControllerModel =
    ModalCoordinator(
        hostModel = this,
        controllersFactory = controllersFactory
    )
