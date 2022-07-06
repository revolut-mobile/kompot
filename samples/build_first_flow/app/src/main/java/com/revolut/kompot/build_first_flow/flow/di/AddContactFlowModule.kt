package com.revolut.kompot.build_first_flow.flow.di

import com.revolut.kompot.build_first_flow.flow.AddContactFlowContract
import com.revolut.kompot.build_first_flow.flow.AddContactFlowModel
import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class AddContactFlowModule : BaseFlowModule {
    @Binds
    @FlowScope
    abstract fun provideFlowModel(flowModel: AddContactFlowModel): AddContactFlowContract.FlowModelApi
}