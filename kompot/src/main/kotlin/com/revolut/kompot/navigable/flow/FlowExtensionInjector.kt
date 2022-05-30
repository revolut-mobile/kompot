package com.revolut.kompot.navigable.flow

import com.revolut.kompot.di.scope.FlowQualifier
import com.revolut.kompot.navigable.ControllerExtension

interface FlowExtensionInjector {

    @FlowQualifier
    fun getControllerExtensions(): Set<ControllerExtension>
}