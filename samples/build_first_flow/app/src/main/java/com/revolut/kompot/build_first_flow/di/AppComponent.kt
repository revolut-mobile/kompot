package com.revolut.kompot.build_first_flow.di

import com.revolut.kompot.build_first_flow.flow.di.AddContactFlowComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface AppComponent {

    val addContactFlowComponent: AddContactFlowComponent.Builder

}