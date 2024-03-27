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
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.dialog.EmptyDialogModelResult
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import kotlinx.coroutines.flow.Flow

internal class FakeScreenModel : BaseScreenModel<ScreenStates.EmptyDomain, ScreenStates.EmptyUI, IOData.EmptyOutput>(
    mock()
) {
    override val initialState = ScreenStates.EmptyDomain

    var modalScreenResultHandled = false
    var modalFlowResultHandled = false

    init {
        injectDependencies(mock(), mock(), mock())
    }


    fun navigateToDestination(destination: DummyNavigationDestination) {
        destination.navigate()
    }

    fun postError(throwable: Throwable) {
        eventsDispatcher.handleEvent(ErrorEvent(throwable))
    }

    fun startModalScreen() {
        DummyScreen<ScreenStates.EmptyUI, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput).showModal {
            modalScreenResultHandled = true
        }
    }

    fun startModalFlow() {
        DummyFlow<FlowStep, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput).showModal {
            modalFlowResultHandled = true
        }
    }

    fun startDialog(fakeDialogModel: DummyDialogModel): Flow<EmptyDialogModelResult> =
        showDialog(fakeDialogModel)
}