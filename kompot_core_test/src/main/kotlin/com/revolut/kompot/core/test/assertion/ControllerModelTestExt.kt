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

import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.coroutines.test.TestContextProvider
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : ControllerModel> T.applyTestDependencies(
    dialogDisplayer: DialogDisplayer = mock(),
    eventsDispatcher: EventsDispatcher = mock(),
    controllersCache: ControllersCache = FakeControllersCache(),
    mainDispatcher: CoroutineDispatcher = TestContextProvider.unconfinedDispatcher()
): T = apply {
    injectDependencies(
        dialogDisplayer = dialogDisplayer,
        eventsDispatcher = eventsDispatcher,
        controllersCache = controllersCache,
        mainDispatcher = mainDispatcher
    )
}

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.resultStream() = resultsBinder().asFlow()

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.backStream() = backPressBinder().asFlow()