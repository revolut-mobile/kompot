package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.ParentFlowComponent
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.FlowModel
import com.revolut.kompot.navigable.flow.FlowStep

class DummyFlow<STEP : FlowStep, INPUT_DATA : IOData.Input, OUTPUT_DATA : IOData.Output>(input: INPUT_DATA) : BaseFlow<STEP, INPUT_DATA, OUTPUT_DATA>(input) {

    override val component: ParentFlowComponent
        get() = throw NotImplementedError()
    override val flowModel: FlowModel<STEP, OUTPUT_DATA>
        get() = throw NotImplementedError()

    override fun updateUi(step: STEP) {
        throw NotImplementedError()
    }
}