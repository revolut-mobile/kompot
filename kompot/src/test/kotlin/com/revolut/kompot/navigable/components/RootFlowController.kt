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
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.screen.EmptyFlowComponent
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.RootControllerManager
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.flow.EmptyFlowState
import com.revolut.kompot.navigable.root.BaseRootFlowModel
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.ControllerContainerFrameLayout

class TestRootFlow(child: Controller, postponeStateRestore: Boolean = false) : RootFlow<TestStep, IOData.EmptyInput>(IOData.EmptyInput) {

    internal val model = TestRootFlowModel(child, postponeStateRestore)

    override val rootDialogDisplayer: DialogDisplayer = mock()
    override val containerForModalNavigation: ControllerContainerFrameLayout = mock()
    override val flowModel = model
    override val component: BaseFlowComponent = EmptyFlowComponent

    internal val controllerManager = RootControllerManager(
        rootFlow = this,
        activityLauncher = mock(),
        permissionsRequester = mock(),
        defaultControllerContainer = 1,
        controllersCache = DefaultControllersCache(20),
        hooksProvider = mock()
    )

    init {
        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
        mainControllerContainer = ControllerContainerFrameLayout(ApplicationProvider.getApplicationContext())
        mainControllerContainer.containerId = ControllerContainer.MAIN_CONTAINER_ID
        bind(controllerManager, parentController = null)
        model.rootNavigator = mock()
    }

    internal fun show() {
        controllerManager.showRootFlow(
            savedState = null,
            hostContainer = ControllerContainerFrameLayout(ApplicationProvider.getApplicationContext())
        )
    }

    override fun createView(inflater: LayoutInflater): View {
        return view
    }

    override fun onCreateFlowView(view: View) = Unit
}

class TestRootFlowModel(var child: Controller, private val postponeStateRestore: Boolean) : BaseRootFlowModel<EmptyFlowState, TestStep>() {
    override val initialStep = TestStep(1)
    override val initialState = EmptyFlowState

    override fun getController(step: TestStep): Controller = child

    override fun postponeSavedStateRestore(): Boolean = postponeStateRestore
}