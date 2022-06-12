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

import com.revolut.kompot.common.IOData
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.screen.StateMapper
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList

internal class BaseScreenModelTest {

    @Test
    fun `emits ui states created from domain state stream`() = dispatchBlockingTest {
        val screenModel = TestScreenModel(initialState = TestDomainState(1))

        val expected = listOf(TestUIState(1), TestUIState(2), TestUIState(3))

        launch {
            val actual = screenModel.uiStateStream().take(3).toList()
            assertEquals(expected, actual)
        }

        screenModel.pushState { TestDomainState(2) }
        screenModel.pushState { TestDomainState(3) }
    }

    @Test
    fun `distinct ui states created from domain state stream`() = dispatchBlockingTest {
        val screenModel = TestScreenModel(initialState = TestDomainState(1))

        val expected = listOf(TestUIState(1), TestUIState(2))

        launch {
            val actual = screenModel.uiStateStream().take(2).toList()
            assertEquals(expected, actual)
        }

        screenModel.pushState { TestDomainState(1) }
        screenModel.pushState { TestDomainState(2) }
    }

    @Test
    fun `all emission mapped on the same thread`() = dispatchBlockingTest {
        val testStateMapper = TestStateMapper()
        val screenModel = TestScreenModel(
            initialState = TestDomainState(1),
            testStateMapper = testStateMapper
        )

        launch {
            screenModel.uiStateStream().take(2).toList()

            val mappingThreads = testStateMapper.mappingThreads

            assertTrue(mappingThreads[0] == mappingThreads[1])
        }

        screenModel.pushState { TestDomainState(2) }
    }

    @Test
    fun `first domain state emission mapped on default and others on worker thread if background state mapping enabled`() = dispatchBlockingTest {
        val testStateMapper = TestStateMapper()
        val screenModel = TestScreenModel(
            initialState = TestDomainState(1),
            testStateMapper = testStateMapper,
            mapStateInBackground = true
        )

        launch {
            screenModel.uiStateStream().take(2).toList()

            val mappingThreads = testStateMapper.mappingThreads

            assertFalse(mappingThreads[0] == mappingThreads[1])
        }

        screenModel.pushState { TestDomainState(2) }
    }

    @Test
    fun `pass current state to updateState lambda`() {
        val firstState = TestDomainState(1)
        val secondState = TestDomainState(2)
        val screenModel = TestScreenModel(initialState = firstState)

        screenModel.pushState {
            assertEquals(this, firstState)
            secondState
        }
        screenModel.pushState {
            assertEquals(this, secondState)
            TestDomainState(3)
        }
    }

    @Test
    fun `update result stream when postScreenResult invoked`() = dispatchBlockingTest {
        val screenModel = TestScreenModel()

        val expectedResults = listOf(IOData.EmptyOutput, IOData.EmptyOutput)

        launch {
            val actual = screenModel.resultsBinder().asFlow().take(2).toList()
            assertEquals(expectedResults, actual)
        }

        screenModel.postScreenResult(IOData.EmptyOutput)
        screenModel.postScreenResult(IOData.EmptyOutput)
    }

    @Test
    fun `update back stream when postBack invoked`() = dispatchBlockingTest {
        val screenModel = TestScreenModel()

        val actual = mutableListOf<Unit>()

        launch {
            screenModel.backPressBinder().asFlow().take(2).toList(actual)
        }

        screenModel.postBack()
        screenModel.postBack()

        assertTrue(actual.size == 2)
    }

    class TestScreenModel(
        override val initialState: TestDomainState = TestDomainState(-1),
        override val mapStateInBackground: Boolean = false,
        testStateMapper: TestStateMapper = TestStateMapper()
    ) : BaseScreenModel<TestDomainState, TestUIState, IOData.EmptyOutput>(testStateMapper) {

        fun pushState(func: TestDomainState.() -> TestDomainState) {
            updateState(func)
        }

    }

    class TestStateMapper : StateMapper<TestDomainState, TestUIState> {

        val mappingThreads = CopyOnWriteArrayList<String>()

        override fun mapState(domainState: TestDomainState): TestUIState {
            mappingThreads.add(Thread.currentThread().name)
            return TestUIState(domainState.value)
        }
    }

    data class TestDomainState(val value: Int) : ScreenStates.Domain
    data class TestUIState(val value: Int) : ScreenStates.UI

}