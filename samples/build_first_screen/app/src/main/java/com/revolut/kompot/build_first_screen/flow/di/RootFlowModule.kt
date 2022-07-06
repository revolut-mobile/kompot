package com.revolut.kompot.build_first_screen.flow.di

import com.revolut.kompot.build_first_screen.flow.RootFlowContract
import com.revolut.kompot.build_first_screen.flow.RootFlowModel
import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class RootFlowModule : BaseFlowModule {
    @Binds
    @FlowScope
    abstract fun provideFlowModel(flowModel: RootFlowModel): RootFlowContract.FlowModelApi
}