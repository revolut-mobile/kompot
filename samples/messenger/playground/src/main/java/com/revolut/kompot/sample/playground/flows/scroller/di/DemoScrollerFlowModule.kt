package com.revolut.kompot.sample.playground.flows.scroller.di

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.di.flow.scroller.BaseScrollerFlowModule
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowContract
import com.revolut.kompot.sample.playground.flows.scroller.DemoScrollerFlowModel
import dagger.Binds
import dagger.Module

@OptIn(ExperimentalKompotApi::class)
@Module
abstract class DemoScrollerFlowModule : BaseScrollerFlowModule {
    @Binds
    @FlowScope
    abstract fun provideFlowModel(flowModel: DemoScrollerFlowModel): DemoScrollerFlowContract.FlowModelApi
}