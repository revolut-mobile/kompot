package com.revolut.kompot.sample.playground.flows.demo.di

import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.playground.flows.demo.DemoFlowContract
import com.revolut.kompot.sample.playground.screens.demo.di.DemoScreenInjector
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [DemoFlowModule::class]
)
interface DemoFlowComponent : BaseFlowComponent,
    DemoScreenInjector {
    val flowModel: DemoFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<DemoFlowComponent, Builder>
}