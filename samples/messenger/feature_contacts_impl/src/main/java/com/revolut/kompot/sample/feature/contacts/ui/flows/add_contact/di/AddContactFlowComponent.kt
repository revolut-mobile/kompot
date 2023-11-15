package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.di

import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.navigable.vc.di.FlowViewControllerComponent
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.di.InputControllerInjector
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [AddContactFlowModule::class]
)
interface AddContactFlowComponent : FlowViewControllerComponent, InputControllerInjector {
    val model: AddContactFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : FlowViewControllerComponent.Builder<AddContactFlowComponent, Builder>
}