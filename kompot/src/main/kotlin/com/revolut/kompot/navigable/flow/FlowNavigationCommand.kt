package com.revolut.kompot.navigable.flow

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.TransitionAnimation

sealed class FlowNavigationCommand<STEP : FlowStep, OUTPUT : IOData.Output>

data class Next<STEP : FlowStep, OUTPUT : IOData.Output>(val step: STEP, val addCurrentStepToBackStack: Boolean, val animation: TransitionAnimation) :
    FlowNavigationCommand<STEP, OUTPUT>()

class Back<STEP : FlowStep, OUTPUT : IOData.Output> : FlowNavigationCommand<STEP, OUTPUT>()

class Quit<STEP : FlowStep, OUTPUT : IOData.Output> : FlowNavigationCommand<STEP, OUTPUT>()

data class PostFlowResult<STEP : FlowStep, OUTPUT : IOData.Output>(val data: OUTPUT) : FlowNavigationCommand<STEP, OUTPUT>()

class StartPostponedStateRestore<STEP : FlowStep, OUTPUT : IOData.Output>: FlowNavigationCommand<STEP, OUTPUT>()