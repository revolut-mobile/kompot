package com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.di

import com.revolut.kompot.di.flow.BaseFlowModule
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowContract
import com.revolut.kompot.sample.feature.contacts.ui.flows.add_contact.AddContactFlowModel
import dagger.Binds
import dagger.Module

@Module
internal abstract class AddContactFlowModule : BaseFlowModule {
    @Binds
    @FlowScope
    abstract fun bindFlowModel(flowModel: AddContactFlowModel): AddContactFlowContract.FlowModelApi
}