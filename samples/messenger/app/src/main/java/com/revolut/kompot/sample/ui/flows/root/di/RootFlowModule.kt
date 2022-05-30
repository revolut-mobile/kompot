package com.revolut.kompot.sample.ui.flows.root.di

import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.ui.flows.root.RootFlowContract
import com.revolut.kompot.sample.ui.flows.root.RootFlowModel
import dagger.Binds
import dagger.Module

@Module
abstract class RootFlowModule : BaseFlowModule {

    @Binds
    @FlowScope
    abstract fun provideFlowModel(flowModel: RootFlowModel): RootFlowContract.FlowModelApi
}