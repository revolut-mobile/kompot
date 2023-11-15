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

import android.view.LayoutInflater
import android.view.View
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.di.flow.ControllerComponent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ControllerEnvironmentTest {

    @Test
    fun `controller is modal root when it is the only screen in the modal container`() {
        val controller = TestController().apply {
            bind(
                controllerManager = TestControllerManager(modal = true),
                parentController = TestParentController(hasBackStack = false)
            )
        }
        assertTrue(controller.env.modalRoot)
    }

    @Test
    fun `controller is modal root when it is the only screen in the modal flow`() {
        val controller = TestController().apply {
            bind(
                controllerManager = TestControllerManager(modal = false),
                parentController = TestParentController(hasBackStack = false).apply {
                    bind(
                        controllerManager = TestControllerManager(modal = true),
                        parentController = TestParentController(hasBackStack = false)
                    )
                }
            )
        }
        assertTrue(controller.env.modalRoot)
    }

    @Test
    fun `controller is not modal root when it is not displayed in the modal`() {
        val controller = TestController().apply {
            bind(
                controllerManager = TestControllerManager(modal = false),
                parentController = TestParentController(hasBackStack = false)
            )
        }
        assertFalse(controller.env.modalRoot)
    }

    @Test
    fun `controller is not modal root when parent flow has back stack`() {
        val controller = TestController().apply {
            bind(
                controllerManager = TestControllerManager(modal = false),
                parentController = TestParentController(hasBackStack = true).apply {
                    bind(
                        controllerManager = TestControllerManager(modal = true),
                        parentController = TestParentController(hasBackStack = false)
                    )
                }
            )
        }
        assertFalse(controller.env.modalRoot)
    }

    @Suppress("FunctionName")
    private fun TestControllerManager(modal: Boolean): ControllerManager = mock {
        on { this.modal } doReturn modal
    }

    private class TestController : Controller() {
        override val layoutId: Int = 0
        override fun createView(inflater: LayoutInflater): View = mock()
        val env get() = environment
    }

    private class TestParentController(override val hasBackStack: Boolean) : Controller(), ParentFlow {
        override val layoutId: Int = 0
        override fun createView(inflater: LayoutInflater): View = mock()
        override val component: ControllerComponent get() = error("Not available")
    }
}