package com.revolut.kompot.sample.playground.flows.demo.di

import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.playground.flows.demo.DemoFlowContract
import com.revolut.kompot.sample.playground.flows.demo.DemoFlowModel
import dagger.Binds
import dagger.Module

@Module
abstract class DemoFlowModule : BaseFlowModule {

    @Binds
    @FlowScope
    abstract fun provideFlowModel(flowModel: DemoFlowModel): DemoFlowContract.FlowModelApi

}