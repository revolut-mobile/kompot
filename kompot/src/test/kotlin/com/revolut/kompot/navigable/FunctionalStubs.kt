package com.revolut.kompot.navigable

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import kotlinx.parcelize.Parcelize

internal sealed class TestStep : FlowStep {
    @Parcelize
    object Step1 : TestStep()

    @Parcelize
    object Step2 : TestStep()
}

@Parcelize
internal data class TestState(val value: Int) : FlowState

internal class TestFlowModel(
    private val firstStepController: Controller = TestController(),
    private val postponeSavedStateRestore: Boolean = false,
) : BaseFlowModel<TestState, TestStep, IOData.EmptyOutput>() {

    override val initialStep: TestStep = TestStep.Step1
    override val initialState: TestState = TestState(1)

    override fun postponeSavedStateRestore(): Boolean = postponeSavedStateRestore

    override fun getController(step: TestStep): Controller = when (step) {
        TestStep.Step1 -> firstStepController
        TestStep.Step2 -> {
            currentState = TestState(2)
            TestController()
        }
    }

    fun changeState(newValue: Int) {
        currentState = currentState.copy(value = newValue)
    }

}

internal class TestFlow(testFlowModel: TestFlowModel) : BaseFlow<TestStep, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val flowModel: FlowModel<TestStep, IOData.EmptyOutput> = testFlowModel

    override fun updateUi(step: TestStep) = Unit

    override val component: BaseFlowComponent = object : BaseFlowComponent {
        override fun getControllerExtensions(): Set<ControllerExtension> = emptySet()
    }

    init {
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn mock()
        }
        val mockedActivity = mock<Activity> {
            on { window } doReturn mock()
        }
        view = mock {
            on { context } doReturn mockedActivity
        }
        childManagerContainerView = mock()
        val rootFlow: RootFlow<*, *> = mock {
            on { rootDialogDisplayer } doReturn mock()
            on { navActionsScheduler } doReturn NavActionsScheduler()
        }
        bind(parentControllerManager, parentController = rootFlow)
    }

    override fun getChildControllerManager(container: ViewGroup, extraKey: String): ControllerManager = mock()

}

internal class TestController : Controller() {

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
            on { controllersCache } doReturn mock()
        }
        bind(parentControllerManager, parentController = mock())
    }

}