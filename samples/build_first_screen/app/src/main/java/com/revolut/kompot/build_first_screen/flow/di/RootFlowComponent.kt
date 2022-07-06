package com.revolut.kompot.build_first_screen.flow.di

import com.revolut.kompot.build_first_screen.flow.RootFlowContract
import com.revolut.kompot.build_first_screen.screen.di.DemoScreenComponent
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [RootFlowModule::class]
)
interface RootFlowComponent : BaseFlowComponent {
    val flowModel: RootFlowContract.FlowModelApi

    fun getDemoScreenComponentBuilder(): DemoScreenComponent.Builder

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<RootFlowComponent, Builder>
}