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

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.toIntent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.flow.FlowViewController
import com.revolut.kompot.view.ControllerContainer

internal class RootNavigator(private val rootFlow: RootFlow<*, *>) {
    private val addOpenExternalForResultListeners = mutableListOf<() -> Unit>()

    fun openModal(destination: ModalDestination, callerController: Controller, showImmediately: Boolean) {
        when (destination) {
            is ModalDestination.ExplicitScreen<*> -> {
                openModalScreen(destination, callerController, showImmediately)
            }

            is ModalDestination.ExplicitFlow<*> -> {
                openModalFlow(destination, callerController, showImmediately)
            }

            is ModalDestination.ExplicitScrollerFlow<*> -> {
                openModalScrollerFlow(destination, callerController, showImmediately)
            }

            is ModalDestination.CallbackController -> {
                openModalCallbackController(destination, callerController, showImmediately)
            }
        }
    }

    private fun <T : IOData.Output> openModalScreen(
        destination: ModalDestination.ExplicitScreen<T>,
        callerController: Controller,
        showImmediately: Boolean
    ) {
        rootFlow.open(
            controller = destination.screen.apply {
                (this as Controller).run {
                    doOnCreate {
                        (view as ControllerContainer).fitStatusBar = true
                    }
                }
                onScreenResult = { result ->
                    (this as Controller).parentControllerManager.clear()
                    destination.onResult?.invoke(result)
                }
            } as Controller,
            style = destination.style,
            parentController = callerController.getFlow(),
            showImmediately = showImmediately,
        )
    }

    private fun <T : IOData.Output> openModalFlow(
        destination: ModalDestination.ExplicitFlow<T>,
        callerController: Controller,
        showImmediately: Boolean
    ) {
        rootFlow.open(
            controller = destination.flow.apply {
                (this as Controller).run {
                    doOnCreate {
                        (view as ControllerContainer).fitStatusBar = true
                    }
                }
                onFlowResult = { result ->
                    (this as Controller).parentControllerManager.clear()
                    destination.onResult?.invoke(result)
                }
            } as Controller,
            style = destination.style,
            parentController = callerController.getFlow(),
            showImmediately = showImmediately,
        )
    }

    @OptIn(ExperimentalKompotApi::class)
    private fun <T : IOData.Output> openModalScrollerFlow(
        destination: ModalDestination.ExplicitScrollerFlow<T>,
        callerController: Controller,
        showImmediately: Boolean
    ) {
        rootFlow.open(
            controller = destination.flow.apply {
                (this as Controller).run {
                    doOnCreate {
                        (view as ControllerContainer).fitStatusBar = true
                    }
                }
                onFlowResult = { result ->
                    (this as Controller).parentControllerManager.clear()
                    destination.onResult?.invoke(result)
                }
            } as Controller,
            style = destination.style,
            parentController = callerController.getFlow(),
            showImmediately = showImmediately,
        )
    }

    @OptIn(ExperimentalKompotApi::class)
    private fun openModalCallbackController(
        destination: ModalDestination.CallbackController,
        callerController: Controller,
        showImmediately: Boolean
    ) {
        rootFlow.open(
            controller = destination.controller.apply {
                when (this) {
                    is ViewController<*> -> applyViewControllerModalResultHandling()
                    is Screen<*> -> applyScreenModalResultHandling()
                    is Flow<*> -> applyFlowModalResultHandling()
                    is ScrollerFlow<*> -> applyScrollerModalResultHandling()
                    else -> error("Unsupported modal controller type: ${destination.controller::class.java}}")
                }
            },
            style = destination.style,
            parentController = callerController.getFlow(),
            showImmediately = showImmediately,
        )
    }

    private fun <Out : IOData.Output> Screen<Out>.applyScreenModalResultHandling() {
        val originalResultHandler = onScreenResult
        onScreenResult = { result ->
            (this as Controller).parentControllerManager.clear()
            originalResultHandler.invoke(result)
        }
    }

    private fun <Out : IOData.Output> Flow<Out>.applyFlowModalResultHandling() {
        val originalResultHandler = onFlowResult
        onFlowResult = { result ->
            (this as Controller).parentControllerManager.clear()
            originalResultHandler.invoke(result)
        }
    }

    private fun <Out : IOData.Output> ViewController<Out>.applyViewControllerModalResultHandling() {
        val originalResultHandler = onResult
        onResult = { result ->
            (this as Controller).parentControllerManager.clear()
            originalResultHandler.invoke(result)
        }
    }

    @OptIn(ExperimentalKompotApi::class)
    private fun <Out : IOData.Output> ScrollerFlow<Out>.applyScrollerModalResultHandling() {
        val originalResultHandler = onFlowResult
        onFlowResult = { result ->
            (this as Controller).parentControllerManager.clear()
            originalResultHandler.invoke(result)
        }
    }

    private fun Controller.getFlow(): Controller? = if (this is BaseFlow<*, *, *> || this is FlowViewController) {
        this
    } else {
        parentController
    }

    fun openExternal(destination: ExternalDestination, controller: Controller?) {
        if (destination.requestCode == null) {
            (controller ?: rootFlow).startActivity(destination.toIntent(rootFlow.activity))
        } else {
            addOpenExternalForResultListeners.forEach { listener -> listener() }
            (controller ?: rootFlow).startActivityForResult(destination.toIntent(rootFlow.activity), destination.requestCode ?: 0)
        }
    }

    fun addOpenExternalForResultListener(listener: () -> Unit) {
        addOpenExternalForResultListeners.add(listener)
    }

    fun removeOpenExternalForResultListener(listener: () -> Unit) {
        addOpenExternalForResultListeners.remove(listener)
    }

    fun openWebPage(url: String) {
        openExternal(
            ExternalDestination.Browser(url),
            rootFlow
        )
    }

}
