package com.revolut.kompot.sample.playground.flows.scroller.di

import com.revolut.kompot.di.flow.scroller.BaseScrollerFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowContract
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [DemoScrollerFlowModule::class]
)
interface DemoScrollerFlowComponent : BaseScrollerFlowComponent {

    val flowModel: DemoScrollerFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : BaseScrollerFlowComponent.Builder<DemoScrollerFlowComponent, Builder>
}