package com.revolut.kompot.di.flow

import com.revolut.kompot.di.scope.FlowQualifier
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.flow.BaseFlow
import dagger.Binds
import dagger.multibindings.Multibinds

interface BaseFlowModule {
    @Binds
    @FlowScope
    @FlowQualifier
    fun provideController(flow: BaseFlow<*, *, *>): Controller

    @Multibinds
    @FlowScope
    @FlowQualifier
    fun provideControllerExtensions(): Set<ControllerExtension>
}