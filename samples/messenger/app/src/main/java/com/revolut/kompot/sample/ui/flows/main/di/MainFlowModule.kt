package com.revolut.kompot.sample.ui.flows.main.di

import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract
import com.revolut.kompot.sample.ui.flows.main.MainFlowModel
import dagger.Binds
import dagger.Module

@Module
abstract class MainFlowModule : BaseFlowModule {

    @Binds
    @FlowScope
    abstract fun bindsMainFlowModel(
        mainFlowModel: MainFlowModel
    ) : MainFlowContract.FlowModelApi

}