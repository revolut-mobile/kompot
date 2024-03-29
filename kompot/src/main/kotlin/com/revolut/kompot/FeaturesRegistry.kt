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
import com.revolut.kompot.common.ControllerDescriptor
import com.revolut.kompot.common.ControllerHolder
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationRequest
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel

interface FeaturesRegistry {
    fun clearFeatures(context: Context, signOut: Boolean)

    fun registerFeature(feature: FeatureGateway)

    fun registerFeatures(features: List<FeatureGateway>)

    fun registerFeatureHolders(featureHolder: List<FeatureHolder>)

    fun interceptDestination(destination: NavigationDestination): NavigationDestination?

    fun getControllerOrThrow(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller

    fun provideControllerOrThrow(
        descriptor: ControllerDescriptor<*>,
    ): ControllerHolder

    suspend fun getDestinationOrThrow(
        request: NavigationRequest
    ): NavigationDestination
}

class DefaultFeaturesRegistry : FeaturesRegistry {
    private val featureGateways: MutableList<FeatureGateway> = mutableListOf()
    private val featureHolders: MutableList<FeatureHolder> = mutableListOf()

    override fun clearFeatures(context: Context, signOut: Boolean) {
        if (signOut) {
            featureGateways.forEach { gateway ->
                gateway.clearData(context)
            }
            featureHolders.forEach { holder ->
                holder.clearData(context)
            }
        }

        featureGateways.forEach { gateway -> gateway.clearReference() }
        featureGateways.clear()
        featureHolders.forEach { gateway -> gateway.clearReference() }
        featureHolders.clear()
    }

    override fun registerFeatureHolders(featureHolder: List<FeatureHolder>) {
        featureHolders.addAll(featureHolder)
    }

    override fun registerFeature(feature: FeatureGateway) {
        featureGateways.add(feature)
    }

    override fun registerFeatures(features: List<FeatureGateway>) {
        featureGateways.addAll(features)
    }

    override fun getControllerOrThrow(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller =
        featureGateways.firstNotNullOfOrNull { gateway ->
            gateway.getController(destination, flowModel)
        } ?: error("Controller for $destination not found")

    override fun provideControllerOrThrow(descriptor: ControllerDescriptor<*>): ControllerHolder =
        featureGateways.firstNotNullOfOrNull { gateway ->
            gateway.provideController(descriptor)
        } ?: error("Controller for $descriptor not found")

    override fun interceptDestination(destination: NavigationDestination): NavigationDestination? =
        featureGateways.firstNotNullOfOrNull { gateway ->
            gateway.interceptDestination(destination)
        }

    override suspend fun getDestinationOrThrow(request: NavigationRequest): NavigationDestination =
        featureGateways.firstNotNullOfOrNull { gateway ->
            gateway.getDestination(request)
        } ?: error("Destination for $request not found")
}

interface FeatureApi

interface FeatureGateway : FeatureHolder {

    fun getController(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller?

    fun interceptDestination(destination: NavigationDestination): NavigationDestination? = null

    fun <T : IOData.Output> provideController(descriptor: ControllerDescriptor<T>): ControllerHolder? = null

    suspend fun getDestination(request: NavigationRequest): NavigationDestination? = null
}

interface FeatureHolder {

    fun clearReference()

    fun clearData(context: Context) = Unit

}