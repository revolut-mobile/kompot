package com.revolut.kompot.build_first_flow.flow.di

import com.revolut.kompot.build_first_flow.flow.AddContactFlowContract
import com.revolut.kompot.build_first_flow.screen.input.di.InputScreenInjector
import com.revolut.kompot.build_first_flow.screen.text.di.TextScreenComponent
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [AddContactFlowModule::class]
)
interface AddContactFlowComponent : BaseFlowComponent, InputScreenInjector {
    val flowModel: AddContactFlowContract.FlowModelApi

    fun getTextScreenComponentBuilder(): TextScreenComponent.Builder

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<AddContactFlowComponent, Builder>
}