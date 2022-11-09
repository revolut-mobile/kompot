package com.revolut.kompot.sample.playground

import com.revolut.kompot.FeatureGateway
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.playground.di.PlaygroundApiProvider
import com.revolut.kompot.sample.playground.di.PlaygroundArguments
import com.revolut.kompot.sample.playground.flows.demo.DemoFlow

class PlaygroundFeatureGateway(argsProvider: () -> PlaygroundArguments) : FeatureGateway {

    init {
        PlaygroundApiProvider.init(argsProvider)
    }

    override fun getController(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller? = when (destination) {
        is DemoFlowDestination -> DemoFlow()
        else -> null
    }

    override fun clearReference() {
        PlaygroundApiProvider.clear()
    }

}