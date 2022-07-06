package com.revolut.kompot.sample.playground.flows.demo

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.playground.flows.demo.DemoFlowContract.*
import com.revolut.kompot.sample.playground.screens.demo.DemoScreen
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenContract
import javax.inject.Inject

class DemoFlowModel @Inject constructor() : BaseFlowModel<State, Step, IOData.EmptyOutput>(), FlowModelApi {

    override val initialStep: Step = Step.Step1
    override val initialState: State = State()

    override fun getController(step: Step): Controller = when(step) {
        Step.Step1 -> DemoScreen(
            inputData = DemoScreenContract.InputData(
                title = "Screen 1",
                counter = 1,
                highlighted = true
            )
        ).apply {
            onScreenResult = {
                next(Step.Step2, true)
            }
        }
        Step.Step2 -> DemoScreen(
            inputData = DemoScreenContract.InputData(
                title = "Screen 2",
                counter = 2,
                highlighted = true
            )
        ).apply {
            onScreenResult = {
                postFlowResult(IOData.EmptyOutput)
            }
        }
    }

}