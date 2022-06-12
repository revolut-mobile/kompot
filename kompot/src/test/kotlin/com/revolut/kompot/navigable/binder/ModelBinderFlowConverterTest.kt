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

package com.revolut.kompot.navigable.binder

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.dispatchBlockingTest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModelBinderFlowConverterTest {

    @Test
    fun `should pass binder events to observing flow`() = dispatchBlockingTest {
        val binder = ModelBinder<Int>()

        val actualValues = mutableListOf<Int>()
        launch {
            binder.asFlow().toList(actualValues)
        }

        binder.notify(1)
        binder.notify(2)

        val expectedValues = listOf(1, 2)

        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `should release binding when observing flow is canceled`() = dispatchBlockingTest {
        val mockedBinding = mock<Binding>()
        val binder = mock<ModelBinder<Int>> {
            on { bind(any()) } doReturn mockedBinding
        }

        binder.asFlow().launchIn(this).cancel()

        verify(mockedBinding).clear()
    }

}