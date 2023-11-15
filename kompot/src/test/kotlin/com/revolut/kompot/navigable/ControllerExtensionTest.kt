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

import androidx.lifecycle.Lifecycle
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ControllerExtensionTest {

    @Test
    fun `onParentLifecycleEvent calls corresponding extension's lifecycle events`() {
        with(TestControllerExtension()) {
            onCreate()
            onAttach()
            onDetach()
            onDestroy()

            assertTrue(onCreateCalled)
            assertTrue(onAttachCalled)
            assertTrue(onDetachCalled)
            assertTrue(onDestroyCalled)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `GIVEN scope injected WHEN observe clicks THEN clicks consumed`() = runTest(UnconfinedTestDispatcher()) {
        val clicksProvider = mock<ClicksProvider>()
        whenever(clicksProvider.observeClicks()).thenReturn(flowOf(42))
        with(TestControllerWithClickListener(clicksProvider)) {
            init(this@runTest)
            onParentLifecycleEvent(Lifecycle.Event.ON_RESUME)

            observeClicks()

            assertEquals(consumedClick, 42)
        }
    }

    @Test
    fun `GIVEN no scope WHEN observe clicks THEN collect till detach view throws an error`() {
        val clicksProvider = mock<ClicksProvider>()
        whenever(clicksProvider.observeClicks()).thenReturn(flowOf())
        with(TestControllerWithClickListener(clicksProvider)) {
            onParentLifecycleEvent(Lifecycle.Event.ON_RESUME)

            assertThrows<UninitializedPropertyAccessException> {
                observeClicks()
            }
        }
    }

    open inner class TestControllerExtension : ControllerExtension() {
        var onCreateCalled: Boolean = false
        var onAttachCalled: Boolean = false
        var onDetachCalled: Boolean = false
        var onDestroyCalled: Boolean = false
        override fun onCreate() {
            onCreateCalled = true
        }

        override fun onAttach() {
            onAttachCalled = true
        }

        override fun onDetach() {
            onDetachCalled = true
        }

        override fun onDestroy() {
            onDestroyCalled = true
        }

    }

    internal interface ClicksProvider {
        fun observeClicks(): Flow<Int>
    }

    inner class TestControllerWithClickListener(
        private val clicksProvider: ClicksProvider,
    ) : TestControllerExtension() {

        var consumedClick = 0

        fun observeClicks() {
            clicksProvider.observeClicks()
                .onEach { consumedClick = it }
                .collectTillDetachView()
        }
    }

}