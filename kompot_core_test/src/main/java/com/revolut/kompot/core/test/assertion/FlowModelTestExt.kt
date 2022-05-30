package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowStep

fun <STEP : FlowStep, OUTPUT : IOData.Output> FlowModel<STEP, OUTPUT>.navigationCommandsStream() = navigationBinder().asFlow()