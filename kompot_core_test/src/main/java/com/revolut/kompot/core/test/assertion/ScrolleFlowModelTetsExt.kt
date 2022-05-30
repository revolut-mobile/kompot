package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlowModel

fun <STEP : FlowStep, OUTPUT : IOData.Output> ScrollerFlowModel<STEP, OUTPUT>.navigationCommandsStream() = navigationBinder().asFlow()