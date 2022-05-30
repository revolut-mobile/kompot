package com.revolut.kompot.navigable.flow.scroller

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.scroller.steps.StepsChangeCommand
import kotlinx.coroutines.flow.Flow

@ExperimentalKompotApi
interface ScrollerFlowModel<
        STEP : FlowStep,
        OUTPUT_DATA : IOData.Output
        > {

    fun stepsCommands(): Flow<StepsChangeCommand<STEP>>

    fun getController(step: STEP): Controller

    fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT_DATA>>
}