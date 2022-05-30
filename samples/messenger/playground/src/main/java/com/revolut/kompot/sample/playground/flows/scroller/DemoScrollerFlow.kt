package com.revolut.kompot.sample.playground.flows.scroller

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.scroller.BaseScrollerFlow
import com.revolut.kompot.sample.playground.di.PlaygroundApiProvider
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowContract.Step
import com.revolut.kompot.sample.playground.flows.scroller.di.DemoScrollerFlowComponent
import timber.log.Timber

class DemoScrollerFlow : BaseScrollerFlow<Step, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val component: DemoScrollerFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        PlaygroundApiProvider.component
            .getDemoScrollerFlowComponentBuilder()
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }

    override fun onFullyVisibleStepPositionChanged(step: Step) {
        Timber.d("step: %s", step)
    }
}