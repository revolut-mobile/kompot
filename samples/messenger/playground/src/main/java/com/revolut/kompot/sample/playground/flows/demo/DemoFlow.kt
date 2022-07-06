package com.revolut.kompot.sample.playground.flows.demo

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.sample.playground.di.PlaygroundApiProvider
import com.revolut.kompot.sample.playground.flows.demo.di.DemoFlowComponent

class DemoFlow : BaseFlow<DemoFlowContract.Step, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val flowModel by lazy {
        component.flowModel
    }

    override val component: DemoFlowComponent by lazy {
        (PlaygroundApiProvider.component)
            .getDemoFlowComponentBuilder()
            .flow(this)
            .build()
    }

    override fun updateUi(step: DemoFlowContract.Step) = Unit

}