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
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep

abstract class BaseRootFlowModel<STATE : FlowState, STEP : FlowStep> : BaseFlowModel<STATE, STEP, IOData.EmptyOutput>() {
    open val onExternalActivityOpened: () -> Unit = {}
    internal lateinit var rootNavigator: RootNavigator

    override fun onCreated() {
        super.onCreated()

        rootNavigator.addOpenExternalForResultListener(onExternalActivityOpened)
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
                is ModalDestination -> rootNavigator.openModal(event.destination, event.controller)
                is ExternalDestination -> handleExternalDestination(event.destination, event.controller)
                else -> return super.tryHandleEvent(event)
            }

            return NavigationEventHandledResult
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
}