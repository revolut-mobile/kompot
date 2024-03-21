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

package com.revolut.kompot.navigable.vc.ui

import android.os.Bundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.coroutines.test.TestDispatcherExtension
import com.revolut.kompot.navigable.components.TestDomainState
import com.revolut.kompot.navigable.components.TestPersistentDomainState
import com.revolut.kompot.navigable.components.TestUIPersistentStatesViewControllerModel
import com.revolut.kompot.navigable.components.TestUIState
import com.revolut.kompot.navigable.components.TestUIStatesViewController
import com.revolut.kompot.navigable.components.TestUIStatesViewControllerModel
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.hooks.PersistentModelStateStorageHook
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class UIStatesControllerTest {

    private val model = TestUIStatesViewControllerModel()
    private val screen = TestUIStatesViewController(model)

    @Test
    fun `GIVEN initial state WHEN attached THEN render initial state`() {
        screen.onCreate()
        screen.onAttach()

        assertEquals(1, model.state.current.value)
        screen.assertRenderedStates(listOf(TestUIState(1)))
    }

    @Test
    fun `GIVEN initial state WHEN state updated THEN render updated states`() {
        screen.onCreate()
        screen.onAttach()

        model.state.update { TestDomainState(2) }
        model.state.update { TestDomainState(3) }
        model.state.update { TestDomainState(4) }

        assertEquals(4, model.state.current.value)
        screen.assertRenderedStates(
            listOf(
                TestUIState(1),
                TestUIState(2),
                TestUIState(3),
                TestUIState(4),
            )
        )
    }

    @Test
    fun `GIVEN screen with updated state WHEN restore from saved state THEN restore latest state`() {
        screen.onCreate()
        screen.onAttach()

        model.state.update { TestDomainState(2) }

        val bundle = Bundle()
        screen.saveState(bundle)

        val restoredModel = TestUIStatesViewControllerModel()
        val restoredScreen = TestUIStatesViewController(restoredModel)

        restoredScreen.restoreState(bundle)
        restoredScreen.onCreate()
        restoredScreen.onAttach()

        assertEquals(2, restoredModel.state.current.value)
        restoredScreen.assertRenderedStates(
            listOf(
                TestUIState(2),
            )
        )
    }

    @Test
    fun `GIVEN mapStateInBackground is false THEN all emissions on default`() = com.revolut.kompot.coroutines.test.dispatchBlockingTest {
        val model = TestUIStatesViewControllerModel(mapStatesInBackground = false)
        val screen = TestUIStatesViewController(model)
        screen.onCreate()
        screen.onAttach()
        model.state.update { copy(value = 2) }
        val mappingThreads = model.mapper.mappingThreads
        Assertions.assertTrue(mappingThreads[0] == mappingThreads[1])
    }

    @Test
    fun `GIVEN stateReducer for the persistent state THEN reduced state will be emitted`() = com.revolut.kompot.coroutines.test.dispatchBlockingTest {

        val storage: PersistentModelStateStorage = mock {
            on { get<TestPersistentDomainState>(PersistentModelStateKey(TestUIPersistentStatesViewControllerModel.STORAGE_KEY)) } doReturn TestPersistentDomainState(2)
        }
        val hookProvider: HooksProvider = mock {
            on { getHook(PersistentModelStateStorageHook.Key) } doReturn PersistentModelStateStorageHook(storage)
        }

        val model = TestUIPersistentStatesViewControllerModel(
            initialState = TestPersistentDomainState(1),
            stateReducer = { _, restoredState ->
                restoredState.copy(value = 3)
            }
        )
        val screen = TestUIStatesViewController(
            model,
            hookProvider
        )
        screen.onCreate()
        screen.onAttach()
        screen.assertRenderedStates(
            listOf(
                TestUIState(3),
            )
        )
    }

    @Test
    fun `GIVEN mapStateInBackground is true THEN first state emission mapped on default and others on worker thread`() {
        val dispatcherExtension = TestDispatcherExtension()
        dispatcherExtension.beforeAll(null)
        com.revolut.kompot.coroutines.test.dispatchBlockingTest {
            val model = TestUIStatesViewControllerModel(mapStatesInBackground = true)
            val screen = TestUIStatesViewController(model)
            screen.onCreate()
            screen.onAttach()
            model.state.update { copy(value = 2) }
            model.state.update { copy(value = 3) }
            val mappingThreads = model.mapper.mappingThreads
            //same thread
            Assertions.assertTrue(mappingThreads[0] == mappingThreads[1])
            //different thread after onAttach
            Assertions.assertTrue(mappingThreads[0] != mappingThreads[2])
        }
        dispatcherExtension.afterAll(null)
    }

    private fun TestUIStatesViewController<*>.assertRenderedStates(states: List<TestUIState>) {
        assertEquals(states, renderedStates)
    }
}