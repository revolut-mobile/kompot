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
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.di.EmptyViewControllerComponent
import com.revolut.kompot.navigable.vc.flow.FlowCoordinator
import com.revolut.kompot.navigable.vc.flow.FlowModelBindingImpl
import com.revolut.kompot.navigable.vc.flow.FlowViewController
import com.revolut.kompot.navigable.vc.flow.FlowViewModel
import com.revolut.kompot.navigable.vc.flow.ModelBinding
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.ControllerContainerFrameLayout

internal class TestFlowViewController(
    internal val model: TestFlowViewControllerModel,
) : ViewController<IOData.EmptyOutput>(), FlowViewController {

    override val controllerModel = model
    override val modelBinding by lazy { ModelBinding(controllerModel) }
    override val component = EmptyViewControllerComponent

    @Suppress("unchecked_cast")
    private val modelBindingImpl get() = modelBinding as FlowModelBindingImpl<TestFlowViewControllerModel, TestFlowStep, IOData.EmptyOutput>
    internal val mainControllerManager: ControllerManager
        get() {
            val mainContainer = checkNotNull(modelBindingImpl.mainControllerContainer)
            return modelBindingImpl.getOrCreateChildControllerManager(
                controllerContainer = mainContainer,
                id = mainContainer.containerId,
            )
        }

    internal val currentController get() = mainControllerManager.activeController

    init {
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn DefaultControllersCache(20)
        }
        val rootFlow: RootFlow<*, *> = mock {
            on { rootDialogDisplayer } doReturn mock()
            on { navActionsScheduler } doReturn NavActionsScheduler()
        }
        bind(parentControllerManager, parentController = rootFlow)

        val mainControllerContainer = ControllerContainerFrameLayout(ApplicationProvider.getApplicationContext())
        modelBindingImpl.mainControllerContainer = mainControllerContainer
        mainControllerContainer.containerId = ControllerContainer.MAIN_CONTAINER_ID

        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
    }

    override fun createView(inflater: LayoutInflater): View = view
}

internal class TestFlowViewControllerModel
    : ViewControllerModel<IOData.EmptyOutput>(), FlowViewModel<TestFlowStep, IOData.EmptyOutput> {

    override val flowCoordinator = FlowCoordinator(TestStep(1)) { step ->
        TestViewController(input = step.value.toString())
    }
}