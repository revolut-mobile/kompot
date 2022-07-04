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

package com.revolut.kompot

import android.content.Context
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowStep
import javax.inject.Inject

interface FeaturesManager {
    fun clearFeatures(context: Context, signOut: Boolean)

    fun registerFeature(delegate: FeatureHandlerDelegate<*, *, *>)

    fun registerFeatures(delegates: List<FeatureHandlerDelegate<*, *, *>>)

    fun registerDataFeatures(delegates: List<DataFeatureHandlerDelegate<*, *>>)

    fun getController(featureStep: FeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller

    fun handleDestination(destination: NavigationDestination): DestinationHandlingResult?
}

class FeaturesManagerImpl @Inject constructor() : FeaturesManager {
    private val featureDelegates: MutableList<FeatureHandlerDelegate<*, *, *>> = mutableListOf()
    private val dataFeatureDelegates: MutableList<DataFeatureHandlerDelegate<*, *>> = mutableListOf()

    override fun clearFeatures(context: Context, signOut: Boolean) {
        if (signOut) {
            featureDelegates.forEach { delegate ->
                delegate.clearData(context)
            }
            dataFeatureDelegates.forEach { delegate ->
                delegate.clearData(context)
            }
        }

        featureDelegates.forEach { delegate -> delegate.clearReference() }
        featureDelegates.clear()
        dataFeatureDelegates.forEach { delegate -> delegate.clearReference() }
        dataFeatureDelegates.clear()
    }

    override fun registerDataFeatures(delegates: List<DataFeatureHandlerDelegate<*, *>>) {
        dataFeatureDelegates.addAll(delegates)
    }

    override fun registerFeature(delegate: FeatureHandlerDelegate<*, *, *>) {
        featureDelegates.add(delegate)
    }

    override fun registerFeatures(delegates: List<FeatureHandlerDelegate<*, *, *>>) {
        featureDelegates.addAll(delegates)
    }

    override fun getController(featureStep: FeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller =
        featureDelegates.first { delegate -> delegate.canHandleFeatureFlowStep(featureStep) }.getControllerOrThrow(featureStep, flowModel)

    override fun handleDestination(destination: NavigationDestination): DestinationHandlingResult? {
        featureDelegates.forEach { delegate ->
            delegate.handleDestination(destination)?.run {
                return this
            }
        }

        return null
    }
}

interface FeatureInitialisationArgs

interface FeatureApi

abstract class FeatureHandlerDelegate<Args : FeatureInitialisationArgs, out Api : FeatureApi, in Step : FeatureFlowStep>(
    argsProvider: () -> Args
) : DataFeatureHandlerDelegate<Args, Api>(argsProvider) {

    fun getControllerOrThrow(featureStep: FeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller = getController(featureStep as Step, flowModel)

    abstract fun canHandleFeatureFlowStep(featureStep: FeatureFlowStep): Boolean

    protected abstract fun getController(step: Step, flowModel: BaseFlowModel<*, *, *>): Controller

    abstract fun handleDestination(destination: NavigationDestination): DestinationHandlingResult?
}

abstract class DataFeatureHandlerDelegate<Args, out Api>(protected val argsProvider: () -> Args) {

    abstract fun clearReference()

    open fun clearData(context: Context) = Unit

}

interface FeatureFlowStep : FlowStep

data class DestinationHandlingResult(
    val step: FeatureFlowStep,
    val animation: TransitionAnimation? = null
)