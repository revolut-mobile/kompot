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
import com.revolut.kompot.navigable.components.TestFlowModel
import com.revolut.kompot.navigable.components.TestViewController
import com.revolut.kompot.navigable.flow.BaseFlowModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class DefaultFeatureRegistryTest {

    private val gateway1 = FakeFeatureGateway(gatewayController = null)
    private val gateway2 = FakeFeatureGateway(gatewayController = null)
    private val gateway3WithContent = FakeFeatureGateway(
        gatewayController = TestViewController("", instrumented = false),
        destination = object : NavigationDestination {}
    )

    private val registry = DefaultFeaturesRegistry().apply {
        registerFeatureHolders(listOf(gateway1))
        registerFeatures(listOf(gateway2, gateway3WithContent))
    }

    @Test
    fun `GIVEN gateways AND no sign out WHEN clear THEN clear references only`() {
        registry.clearFeatures(mock(), false)

        assertTrue(gateway1.referenceCleared)
        assertTrue(gateway2.referenceCleared)
        assertTrue(gateway3WithContent.referenceCleared)

        assertFalse(gateway1.dataCleared)
        assertFalse(gateway2.dataCleared)
        assertFalse(gateway3WithContent.dataCleared)
    }

    @Test
    fun `GIVEN gateways AND sign out WHEN clear THEN clear references and data`() {
        registry.clearFeatures(mock(), true)

        assertTrue(gateway1.referenceCleared)
        assertTrue(gateway2.referenceCleared)
        assertTrue(gateway3WithContent.referenceCleared)

        assertTrue(gateway1.dataCleared)
        assertTrue(gateway2.dataCleared)
        assertTrue(gateway3WithContent.dataCleared)
    }

    @Test
    fun `GIVEN gateways WHEN getControllerOrThrow THEN get first available controller`() {
        val navDestination = object : NavigationDestination {}
        val controller = registry.getControllerOrThrow(navDestination, TestFlowModel())
        assertEquals(gateway3WithContent.gatewayController, controller)
    }

    @Test
    fun `GIVEN gateways WHEN provideController by descriptor THEN provide first available controller`() {
        val descriptor = object : ControllerDescriptor<IOData.Output> {}
        val result = registry.provideControllerOrThrow(descriptor)
        assertEquals(ControllerHolder(gateway3WithContent.gatewayController!!), result)
    }

    @Test
    fun `GIVEN gateways WHEN interceptDestination THEN get first intercepted destination`() {
        val navDestination = object : NavigationDestination {}
        val interceptedDestination = registry.interceptDestination(navDestination)
        assertEquals(gateway3WithContent.destination, interceptedDestination)
    }

    @Test
    fun `GIVEN gateways WHEN getDestination THEN get first available destination`() = dispatchBlockingTest {
        val navigationRequest = object : NavigationRequest {}
        val resolvedDestination = registry.getDestinationOrThrow(navigationRequest)
        assertEquals(gateway3WithContent.destination, resolvedDestination)
    }

    private class FakeFeatureGateway(
        val gatewayController: TestViewController? = null,
        val destination: NavigationDestination? = null,
    ) : FeatureGateway {
        var dataCleared = false
            private set
        var referenceCleared = false
            private set

        override fun getController(destination: NavigationDestination, flowModel: BaseFlowModel<*, *, *>): Controller? = gatewayController

        override fun <T : IOData.Output> provideController(descriptor: ControllerDescriptor<T>): ControllerHolder? =
            gatewayController?.let { ControllerHolder(it) }

        override fun interceptDestination(destination: NavigationDestination): NavigationDestination? =
            this.destination

        override suspend fun getDestination(request: NavigationRequest): NavigationDestination? =
            destination

        override fun clearData(context: Context) {
            dataCleared = true
        }

        override fun clearReference() {
            referenceCleared = true
        }
    }
}