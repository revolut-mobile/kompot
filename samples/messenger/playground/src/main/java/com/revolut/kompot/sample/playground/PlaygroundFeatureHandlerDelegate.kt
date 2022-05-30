package com.revolut.kompot.sample.playground

import com.revolut.kompot.DestinationHandlingResult
import com.revolut.kompot.FeatureFlowStep
import com.revolut.kompot.FeatureHandlerDelegate
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.playground.di.PlaygroundApiProvider
import com.revolut.kompot.sample.playground.di.PlaygroundArguments
import com.revolut.kompot.sample.playground.flows.demo.DemoFlow
import kotlinx.parcelize.Parcelize

class PlaygroundFeatureHandlerDelegate(
    argsProvider: () -> PlaygroundArguments
) : FeatureHandlerDelegate<PlaygroundArguments, PlaygroundApi, PlaygroundFeatureFlowStep>(argsProvider)  {

    init {
        PlaygroundApiProvider.init(argsProvider)
    }

    override fun getFeatureApi(): PlaygroundApi = PlaygroundApiProvider.component

    override fun canHandleFeatureFlowStep(featureStep: FeatureFlowStep): Boolean =
        featureStep is PlaygroundFeatureFlowStep

    override fun getController(step: PlaygroundFeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller = when (step) {
        PlaygroundFeatureFlowStep.DemoFlow -> DemoFlow()
    }

    override fun handleDestination(destination: NavigationDestination): DestinationHandlingResult? = when (destination) {
        is DemoFlowDestination -> DestinationHandlingResult(PlaygroundFeatureFlowStep.DemoFlow)
        else -> null
    }

    override fun clearReference() {
        PlaygroundApiProvider.clear()
    }

}

sealed class PlaygroundFeatureFlowStep : FeatureFlowStep {
    @Parcelize
    object DemoFlow : PlaygroundFeatureFlowStep()
}