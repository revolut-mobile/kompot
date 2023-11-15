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

package com.revolut.kompot.navigable.components

import android.app.Activity
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.di.EmptyViewControllerComponent
import com.revolut.kompot.navigable.vc.ui.ModelBinding
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.SaveStateDelegate
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import kotlinx.parcelize.Parcelize

internal class TestUIStatesViewController(
    model: TestUIStatesViewControllerModel
) : ViewController<IOData.EmptyOutput>(), UIStatesController<TestUIState> {

    internal val renderedStates = mutableListOf<TestUIState>()

    override val controllerModel = model
    override val modelBinding by lazy { ModelBinding(controllerModel) }
    override val component = EmptyViewControllerComponent
    override val layoutId: Int = 0

    override fun render(uiState: TestUIState, payload: Any?) {
        renderedStates += uiState
    }

    init {
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn DefaultControllersCache(20)
        }
        val rootFlow: RootFlow<*, *> = mock {
            on { rootDialogDisplayer } doReturn mock()
            on { navActionsScheduler } doReturn NavActionsScheduler()
        }
        bind(parentControllerManager, parentController = rootFlow)

        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
    }

}

internal class TestUIStatesViewControllerModel
    : ViewControllerModel<IOData.EmptyOutput>(), UIStatesModel<TestDomainState, TestUIState, IOData.EmptyOutput> {

    override val state = ModelState(
        initialState = TestDomainState(
            value = 1,
        ),
        stateMapper = TestStateMapper(),
        saveStateDelegate = ModelSaveStateDelegate()
    )

    private class ModelSaveStateDelegate : SaveStateDelegate<TestDomainState, TestRetainedDomainState>() {
        override fun getRetainedState(currentState: TestDomainState): TestRetainedDomainState =
            TestRetainedDomainState(currentState.value)

        override fun restoreDomainState(initialState: TestDomainState, retainedState: TestRetainedDomainState) =
            TestDomainState(retainedState.value)
    }
}

internal class TestStateMapper : States.Mapper<TestDomainState, TestUIState> {
    override fun mapState(domainState: TestDomainState) = TestUIState(domainState.value)
}

data class TestDomainState(val value: Int) : States.Domain
data class TestUIState(val value: Int) : States.UI

@Parcelize
data class TestRetainedDomainState(val value: Int) : States.RetainedDomain