package com.revolut.kompot.build_first_screen.di

import com.revolut.kompot.build_first_screen.flow.di.RootFlowComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface AppComponent {

    val rootFlowComponent: RootFlowComponent.Builder

}