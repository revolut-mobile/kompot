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
import com.revolut.kompot.navigable.components.TestDomainState
import com.revolut.kompot.navigable.components.TestUIState
import com.revolut.kompot.navigable.components.TestUIStatesViewController
import com.revolut.kompot.navigable.components.TestUIStatesViewControllerModel
import org.junit.Assert.assertEquals
import org.junit.Test
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

    private fun TestUIStatesViewController.assertRenderedStates(states: List<TestUIState>) {
        assertEquals(states, renderedStates)
    }
}