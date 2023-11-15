package com.revolut.kompot.sample.playground.flows.scroller

import android.graphics.Color
import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.scroller.BaseScrollerFlowModel
import com.revolut.kompot.navigable.flow.scroller.steps.Steps
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowContract.FlowModelApi
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowContract.Step
import com.revolut.kompot.sample.playground.screens.demo.DemoScreen
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenContract
import javax.inject.Inject

@OptIn(ExperimentalKompotApi::class)
class DemoScrollerFlowModel @Inject constructor() :
    BaseScrollerFlowModel<Step, IOData.EmptyOutput>(), FlowModelApi {

    override val initialSteps = Steps(Step.FirstStep, Step.SecondStep, Step.ThirdStep)

    override fun getController(step: Step): Controller = when (step) {
        Step.FirstStep -> makeScreen(count = 1, color = Color.RED)
        Step.SecondStep -> makeScreen(count = 2, color = Color.GREEN)
        Step.ThirdStep -> makeScreen(count = 3, color = Color.BLUE)
    }

    private fun makeScreen(
        count: Int = 1,
        color: Int = Color.WHITE
    ) = DemoScreen(
        inputData = DemoScreenContract.InputData(
            title = "Nested Flow Screen $count",
            counter = count,
            highlighted = true,
            color = color
        )
    ).apply {
        onScreenResult = {
            DemoScreen(
                inputData = DemoScreenContract.InputData(
                    title = "Modal Screen",
                    counter = 10,
                    highlighted = true,
                    color = Color.MAGENTA
                )
            ).showModal()
        }
    }
}