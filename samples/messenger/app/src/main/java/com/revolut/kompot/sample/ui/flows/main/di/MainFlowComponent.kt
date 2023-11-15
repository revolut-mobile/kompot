package com.revolut.kompot.sample.ui.flows.main.di

import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract
import dagger.Subcomponent

@ViewControllerScope
@Subcomponent(
    modules = [MainFlowModule::class]
)
interface MainFlowComponent : ViewControllerComponent {
    val flowModel: MainFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : ViewControllerComponent.Builder<MainFlowComponent, Builder>
}