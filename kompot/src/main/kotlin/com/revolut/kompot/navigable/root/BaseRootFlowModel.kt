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

package com.revolut.kompot.navigable.root

import androidx.annotation.VisibleForTesting
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.common.NavigationEventHandledResult
import com.revolut.kompot.utils.PostponedRestorationTriggeredEvent
import com.revolut.kompot.utils.EventHandledResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.modal.ModalRestorationRequest

abstract class BaseRootFlowModel<STATE : FlowState, STEP : FlowStep> : BaseFlowModel<STATE, STEP, IOData.EmptyOutput>() {
    open val onExternalActivityOpened: () -> Unit = {}
    internal lateinit var rootNavigator: RootNavigator

    private val modalRestorationRequests = mutableListOf<ModalRestorationRequest>()
    private var controllersFirstLayoutConsumed = false

    override fun onCreated() {
        super.onCreated()

        rootNavigator.addOpenExternalForResultListener(onExternalActivityOpened)
    }

    //Callback introduced to manage modals saved state because it's error prone to trigger modals creation while flows are attaching.
    //Doing so results in lifecycle events conflict (Modal tries to cover a flow while its onAttach method is still in process)
    //It is safe to call modals restoration after initial controllers hierarchy is laid out
    internal fun onControllersFirstLayout() {
        restoreModals()
        controllersFirstLayoutConsumed = true
    }

    override fun onFinished() {
        super.onFinished()

        rootNavigator.removeOpenExternalForResultListener(onExternalActivityOpened)
    }

    override fun tryHandleEvent(event: Event): EventResult? {
        //First we handle navigation in successors of BaseRootFlowModel
        //and if it's not handled we handle the navigation here.
        //This way the logic in successors in handleNavigationDestination will be handled first
        //that keeps the behaviour consistent with handleNavigationDestination logic.
        val superResult = super.tryHandleEvent(event)
        if (superResult != null) return superResult

        if (event is NavigationEvent) {
            when (event.destination) {
                is ModalDestination -> rootNavigator.openModal(event.destination, event.controller, showImmediately = false)
                is ExternalDestination -> handleExternalDestination(event.destination, event.controller)
                else -> return super.tryHandleEvent(event)
            }

            return NavigationEventHandledResult
        }
        if (event is ModalRestorationRequest) {
            modalRestorationRequests.add(event)
            return EventHandledResult
        }
        if (event is PostponedRestorationTriggeredEvent) {
            if (controllersFirstLayoutConsumed) {
                restoreModals()
            }
            return EventHandledResult
        }
        return null
    }

    @VisibleForTesting
    fun setupRootNavigator(rootFlow: RootFlow<*, *>) {
        rootNavigator = RootNavigator(rootFlow)
    }

    private fun handleExternalDestination(destination: ExternalDestination, controller: Controller?) {
        when (destination) {
            is ExternalDestination.Browser -> rootNavigator.openWebPage(destination.url)
            else -> rootNavigator.openExternal(destination, controller)
        }
    }

    private fun restoreModals() {
        /**
         * Each restored modal can be a host for another modal. In this case, the restored modal
         * will trigger child modal restoration request upon creation. Therefore we poll restoration
         * requests in a loop until all of them are processed
         */
        while (modalRestorationRequests.isNotEmpty()) {
            //Making a sorted copy of requests list to avoid concurrent modifications.
            //Sorting required to restore modals in the correct order
            modalRestorationRequests.sortedBy { it.index }.forEach { request ->
                restoreModal(request)
                modalRestorationRequests.remove(request)
            }
        }
    }

    private fun restoreModal(restorationRequest: ModalRestorationRequest) {
        rootNavigator.openModal(
            ModalDestination.CallbackController(
                controller = restorationRequest.modalController,
                style = restorationRequest.style,
            ),
            callerController = restorationRequest.controller,
            showImmediately = true,
        )
    }
}