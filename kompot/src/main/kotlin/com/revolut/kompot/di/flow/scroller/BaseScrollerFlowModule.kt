package com.revolut.kompot.di.flow.scroller

import com.revolut.kompot.di.scope.FlowQualifier
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.flow.scroller.BaseScrollerFlow
import dagger.Binds
import dagger.multibindings.Multibinds

interface BaseScrollerFlowModule {
    @[Binds FlowScope FlowQualifier]
    fun provideController(flow: BaseScrollerFlow<*, *, *>): Controller

    @[Multibinds FlowScope FlowQualifier]
    fun provideControllerExtensions(): Set<ControllerExtension>
}