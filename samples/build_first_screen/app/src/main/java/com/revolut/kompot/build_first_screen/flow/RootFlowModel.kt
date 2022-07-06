package com.revolut.kompot.build_first_screen.flow

import com.revolut.kompot.build_first_screen.screen.DemoScreen
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.root.BaseRootFlowModel
import javax.inject.Inject

class RootFlowModel @Inject constructor() :
    BaseRootFlowModel<RootFlowContract.State, RootFlowContract.Step>(),
    RootFlowContract.FlowModelApi {

    override val initialStep = RootFlowContract.Step.DemoScreen
    override val initialState = RootFlowContract.State()

    override fun getController(step: RootFlowContract.Step): Controller = when (step) {
        is RootFlowContract.Step.DemoScreen -> DemoScreen("Hello world!")
    }

}