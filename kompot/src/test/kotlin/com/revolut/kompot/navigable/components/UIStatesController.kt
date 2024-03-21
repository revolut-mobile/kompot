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
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.di.EmptyViewControllerComponent
import com.revolut.kompot.navigable.vc.ui.ModelBinding
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.PersistentModelState
import com.revolut.kompot.navigable.vc.ui.PersistentModelStateKey
import com.revolut.kompot.navigable.vc.ui.SaveStateDelegate
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import kotlinx.parcelize.Parcelize
import java.util.concurrent.CopyOnWriteArrayList

internal class TestUIStatesViewController<Domain: States.Domain>(
    model: UIStatesModel<Domain, TestUIState, IOData.EmptyOutput>,
    private val hooksProviderValue: HooksProvider? = null
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
            on { hooksProvider } doReturn hooksProviderValue
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

internal class TestUIStatesViewControllerModel(
    val mapper: TestStateMapper = TestStateMapper(),
    mapStatesInBackground: Boolean = false,
)
    : ViewControllerModel<IOData.EmptyOutput>(), UIStatesModel<TestDomainState, TestUIState, IOData.EmptyOutput> {

    override val state = ModelState(
        initialState = TestDomainState(
            value = 1,
        ),
        stateMapper = mapper,
        saveStateDelegate = ModelSaveStateDelegate(),
        mapStatesInBackground = mapStatesInBackground,
    )

    private class ModelSaveStateDelegate : SaveStateDelegate<TestDomainState, TestRetainedDomainState>() {
        override fun getRetainedState(currentState: TestDomainState): TestRetainedDomainState =
            TestRetainedDomainState(currentState.value)

        override fun restoreDomainState(initialState: TestDomainState, retainedState: TestRetainedDomainState) =
            TestDomainState(retainedState.value)
    }
}

internal class TestUIPersistentStatesViewControllerModel(
    val initialState: TestPersistentDomainState = TestPersistentDomainState(1),
    val stateReducer: (TestPersistentDomainState, TestPersistentDomainState) -> TestPersistentDomainState
)
    : ViewControllerModel<IOData.EmptyOutput>(), UIStatesModel<TestPersistentDomainState, TestUIState, IOData.EmptyOutput> {

    override val state = PersistentModelState(
        key = PersistentModelStateKey(STORAGE_KEY),
        initialState = initialState,
        stateMapper = TestPersistentStateMapper(),
        restoredStateReducer = stateReducer,
    )

    companion object {
        const val STORAGE_KEY = "key"
    }
}

internal class TestStateMapper : States.Mapper<TestDomainState, TestUIState> {

    val mappingThreads = CopyOnWriteArrayList<String>()

    override fun mapState(domainState: TestDomainState): TestUIState {
        mappingThreads.add(Thread.currentThread().name)
        return TestUIState(domainState.value)
    }
}

internal class TestPersistentStateMapper : States.Mapper<TestPersistentDomainState, TestUIState> {

    val mappingThreads = CopyOnWriteArrayList<String>()

    override fun mapState(domainState: TestPersistentDomainState): TestUIState {
        mappingThreads.add(Thread.currentThread().name)
        return TestUIState(domainState.value)
    }
}

data class TestDomainState(val value: Int) : States.Domain
@Parcelize
data class TestPersistentDomainState(val value: Int) : States.PersistentDomain
data class TestUIState(val value: Int) : States.UI

@Parcelize
data class TestRetainedDomainState(val value: Int) : States.PersistentDomain