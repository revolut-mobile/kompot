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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.revolut.kompot.common.ControllerDescriptor
import com.revolut.kompot.common.ControllerHolder
import com.revolut.kompot.common.ControllerRequest
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.coroutines.test.TestContextProvider
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : ControllerModel> T.applyTestDependencies(
    dialogDisplayer: DialogDisplayer = mock(),
    eventsDispatcher: EventsDispatcher = mock(),
    controllersCache: ControllersCache = FakeControllersCache(),
    mainDispatcher: CoroutineDispatcher = TestContextProvider.unconfinedDispatcher(),
    controllerModelExtensions: Set<ControllerModelExtension> = emptySet(),
): T = apply {
    injectDependencies(
        dialogDisplayer = dialogDisplayer,
        eventsDispatcher = eventsDispatcher,
        controllersCache = controllersCache,
        mainDispatcher = mainDispatcher,
        controllerModelExtensions = controllerModelExtensions,
    )
}

fun <OUTPUT : IOData.Output> ViewControllerModel<OUTPUT>.resultStream() = resultsBinder().asFlow()

fun <OUTPUT : IOData.Output> ViewControllerModel<OUTPUT>.backStream() = backPressBinder().asFlow()

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.resultStream() = resultsBinder().asFlow()

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.backStream() = backPressBinder().asFlow()

internal fun <T : IOData.Output> EventsDispatcher.bindDescriptor(descriptor: ControllerDescriptor<T>, controller: ViewController<T>) {
    val controllerRequestResult = mock<ControllerHolder> {
        on { this.controller } doReturn controller
    }
    whenever(handleEvent(ControllerRequest(descriptor)))
        .thenReturn(controllerRequestResult)
}

fun <T : IOData.Output> ControllerModel.bindDescriptor(descriptor: ControllerDescriptor<T>, controller: ViewController<T>) =
    eventsDispatcher.bindDescriptor(descriptor, controller)

fun <T : IOData.Output> ControllerModelExtension.bindDescriptor(descriptor: ControllerDescriptor<T>, controller: ViewController<T>) =
    parentEventsDispatcher.bindDescriptor(descriptor, controller)

fun ControllerModel.assertDialog(model: DialogModel<*>) =
    ControllerModelAssertions.assertDialog(dialogDisplayer, model)

fun ControllerModel.mockDialogResult(forModel: DialogModel<*>, resultToReturn: DialogModelResult) {
    whenever(dialogDisplayer.showDialog(forModel))
        .doReturn(flowOf(resultToReturn))
}