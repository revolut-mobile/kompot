package com.revolut.kompot.sample.ui.flows.main.di

import com.revolut.kompot.navigable.vc.di.ViewControllerModule
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract
import com.revolut.kompot.sample.ui.flows.main.MainFlowModel
import dagger.Binds
import dagger.Module

@Module
abstract class MainFlowModule : ViewControllerModule {

    @[Binds ViewControllerScope]
    abstract fun bindsMainFlowModel(mainFlowModel: MainFlowModel): MainFlowContract.FlowModelApi
}