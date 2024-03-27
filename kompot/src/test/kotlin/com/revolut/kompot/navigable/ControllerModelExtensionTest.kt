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

package com.revolut.kompot.navigable

import com.revolut.kompot.common.LifecycleEvent
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ControllerModelExtensionTest {

    @Test
    fun `onParentLifecycleEvent with CREATED event should call corresponding extension's methods`() {
        val extension = TestControllerModelExtension()
        extension.onParentLifecycleEvent(LifecycleEvent.CREATED)

        assertTrue(extension.onCreatedCalled)
    }

    @Test
    fun `onParentLifecycleEvent with SHOWN event should call corresponding extension's methods`() {
        val extension = TestControllerModelExtension()
        extension.onParentLifecycleEvent(LifecycleEvent.SHOWN)

        assertTrue(extension.onShownCalled)
    }

    inner class TestControllerModelExtension : ControllerModelExtension() {
        var onCreatedCalled: Boolean = false
        var onShownCalled: Boolean = false
        var onHiddenCalled: Boolean = false
        var onFinishedCalled: Boolean = false

        override fun onCreated() {
            onCreatedCalled = true
        }

        override fun onShown() {
            onShownCalled = true
        }

        override fun onHidden() {
            onHiddenCalled = true
        }

        override fun onFinished() {
            onFinishedCalled = true
        }
    }

}