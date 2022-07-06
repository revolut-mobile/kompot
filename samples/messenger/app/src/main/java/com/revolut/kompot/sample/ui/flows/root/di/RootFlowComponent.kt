package com.revolut.kompot.sample.ui.flows.root.di

import com.revolut.kompot.FeaturesManager
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.ui.flows.root.RootFlowContract
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [RootFlowModule::class]
)
interface RootFlowComponent : BaseFlowComponent {
    val flowModel: RootFlowContract.FlowModelApi

    val featureManager: FeaturesManager

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<RootFlowComponent, Builder>
}
