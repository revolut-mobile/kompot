package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.di

import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.di.InputScreenInjector
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [AddContactFlowModule::class]
)
interface AddContactFlowComponent : BaseFlowComponent, InputScreenInjector {
    val flowModel: AddContactFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<AddContactFlowComponent, Builder>
}