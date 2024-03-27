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
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.view.ControllerContainer.Companion.MAIN_CONTAINER_ID
import com.revolut.kompot.view.ControllerContainerFrameLayout
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions

@Parcelize
data class TestState(val value: Int) : FlowState

interface TestFlowStep : FlowStep {
    val value: Int
}

@Parcelize
data class TestStep(override val value: Int) : TestFlowStep

open class TestFlowModel(
    private val firstStepController: Controller = TestController("1"),
    private val postponeSavedStateRestore: Boolean = false,
) : BaseFlowModel<TestState, TestStep, IOData.EmptyOutput>() {

    override val initialStep: TestStep = TestStep(1)
    override val initialState: TestState = TestState(1)
    val currentRestorationState get() = restorationState
    val curState: TestState get() = currentState

    var randomiseControllerKey: Boolean = false

    init {
        @Suppress("LeakingThis")
        injectDependencies(mock(), mock(), mock())
    }

    private val childFlowModel: TestFlowModel by lazy(LazyThreadSafetyMode.NONE) {
        TestFlowModel().apply { this.setInitialState() }
    }

    override fun postponeSavedStateRestore(): Boolean = postponeSavedStateRestore

    override fun getController(step: TestStep): Controller = when (step.value) {
        1 -> firstStepController
        else -> TestController(if (randomiseControllerKey) "${lastRandomisedControllerKey++}" else "${step.value}", controllersCache)
    }

    fun changeState(newValue: Int) {
        currentState = currentState.copy(value = newValue)
    }

    fun simulateNext(stateValue: Int, addCurrentToBackStack: Boolean = true) {
        val step = TestStep(stateValue)
        next(step, addCurrentToBackStack)
        setNextState(step, TransitionAnimation.NONE, addCurrentToBackStack, childFlowModel)

        getController() //triggered by a flow to get controller that corresponds to the state
        currentState = TestState(stateValue)
    }

    fun restoreToStep(stateValue: Int) {
        restoreToStep(
            StepRestorationCriteria.RestoreByStep(
                condition = {
                    (it as TestStep).value == stateValue
                },
                removeCurrent = true
            )
        )
        handleBackStack(immediate = true)
    }

    fun activate() {
        getController()
    }

    fun assertFlowState(stateValue: Int) {
        Assertions.assertEquals(TestStep(stateValue), step)
        Assertions.assertEquals(TestState(stateValue), curState)
    }

    companion object {
        private var lastRandomisedControllerKey = Int.MIN_VALUE
    }
}

internal class TestFlow(
    testFlowModel: TestFlowModel,
    controllersCacheValue: ControllersCache? = null
) : BaseFlow<TestStep, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val flowModel: FlowModel<TestStep, IOData.EmptyOutput> = testFlowModel

    override fun updateUi(step: TestStep) = Unit

    override val component: BaseFlowComponent = EmptyFlowComponent

    init {
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn (controllersCacheValue ?: mock())
        }
        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
        mainControllerContainer = ControllerContainerFrameLayout(ApplicationProvider.getApplicationContext())
        mainControllerContainer.containerId = MAIN_CONTAINER_ID
        val rootFlow: RootFlow<*, *> = mock {
            on { rootDialogDisplayer } doReturn mock()
            on { navActionsScheduler } doReturn NavActionsScheduler()
        }
        bind(parentControllerManager, parentController = rootFlow)
    }

    override fun createView(inflater: LayoutInflater): View {
        return view
    }
}

internal data class TestController(private val strKey: String = "", private val controllersCacheValue: ControllersCache? = null) : Controller() {

    init {
        this.keyInitialization = { ControllerKey(strKey) }
    }

    override val layoutId: Int = 0

    override fun createView(inflater: LayoutInflater): View {
        return view
    }

    init {
        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn (controllersCacheValue ?: mock())
        }
        bind(parentControllerManager, parentController = mock())
    }

}