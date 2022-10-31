/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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