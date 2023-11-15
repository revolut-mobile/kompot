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

package com.revolut.kompot.holder

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.navigable.ChildControllerListener
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.components.TestController
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.ControllerContainerFrameLayout
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ControllerTransactionTest {

    private val onAttachControllerListener = FakeChildControllerListener()
    private val onDetachControllerListener = FakeChildControllerListener()

    private val controllerManager = ControllerManager(
        modal = false,
        defaultFlowLayout = null,
        controllersCache = DefaultControllersCache(1),
        controllerViewHolder = mock {
            on { container } doReturn mock<ControllerContainerFrameLayout>()
        },
        onAttachController = onAttachControllerListener,
        onDetachController = onDetachControllerListener,
    )

    @Test
    fun `GIVEN starting controller attached WHEN onTransitionCreated THEN detach starting controller`() {
        val from = TestController("1").apply {
            onAttach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertTrue(from.detached)
        assertTrue(onDetachControllerListener.invoked)
    }

    @Test
    fun `WHEN onTransitionCreated THEN attach new controller`() {
        val from = TestController("1")
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertTrue(to.attached)
        assertTrue(onAttachControllerListener.invoked)
    }

    @Test
    fun `GIVEN starting controller detached WHEN onTransitionCreated THEN don't detach`() {
        val from = TestController("1").apply {
            onAttach()
            onDetach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertTrue(from.detached)
        assertFalse(onDetachControllerListener.invoked)
    }

    @Test
    fun `GIVEN new controller attached WHEN onTransitionCreated THEN don't attach`() {
        val from = TestController("1")
        val to = TestController("2").apply {
            onAttach()
        }
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertTrue(to.attached)
        assertFalse(onAttachControllerListener.invoked)
    }

    @Test
    fun `GIVEN indefinite transaction AND starting controller attached WHEN onTransitionCreated THEN don't detach`() {
        val from = TestController("1").apply {
            onAttach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertFalse(from.detached)
        assertFalse(onDetachControllerListener.invoked)
    }

    @Test
    fun `GIVEN indefinite transaction WHEN onTransitionCreated THEN attach new controller`() {
        val from = TestController("1")
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionCreated()

        assertTrue(to.attached)
        assertTrue(onAttachControllerListener.invoked)
    }

    @Test
    fun `GIVEN finite transition WHEN onTransitionFinished THEN don't trigger controllers lifecycle`() {
        val from = TestController("1")
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionFinished()

        assertFalse(from.attached)
        assertFalse(from.detached)
        assertFalse(to.attached)
        assertFalse(to.detached)
        assertFalse(onAttachControllerListener.invoked)
        assertFalse(onDetachControllerListener.invoked)
    }

    @Test
    fun `GIVEN indefinite transition AND starting controller attached WHEN onTransitionFinished THEN detach starting controller`() {
        val from = TestController("1").apply {
            onAttach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            indefinite = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionFinished()

        assertTrue(from.detached)
        assertTrue(onDetachControllerListener.invoked)
    }

    @Test
    fun `GIVEN forward transaction WHEN onTransitionCanceled THEN detach AND destroy new controller`() {
        val from = TestController("1")
        val to = TestController("2").apply {
            onAttach()
        }
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            backward = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCanceled()

        assertTrue(to.detached)
        assertTrue(onDetachControllerListener.invoked)
        verify(controllerManager.controllerViewHolder).remove(to.view)
        assertTrue(to.destroyed)
    }

    @Test
    fun `GIVEN forward transaction WHEN onTransitionCanceled THEN attach starting controller`() {
        val from = TestController("1").apply {
            onAttach()
            onDetach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            backward = false,
        )
        controllerManager.onAttach()

        transaction.onTransitionCanceled()

        assertTrue(from.attached)
        assertTrue(onAttachControllerListener.invoked)
    }

    @Test
    fun `GIVEN backward transaction WHEN onTransitionCanceled THEN detach, not destroy new controller`() {
        val from = TestController("1")
        val to = TestController("2").apply {
            onAttach()
        }
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            backward = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionCanceled()

        assertTrue(to.detached)
        assertTrue(onDetachControllerListener.invoked)
        verify(controllerManager.controllerViewHolder).remove(to.view)
        assertFalse(to.destroyed)
    }

    @Test
    fun `GIVEN backward transaction WHEN onTransitionCanceled THEN attach starting controller`() {
        val from = TestController("1").apply {
            onAttach()
            onDetach()
        }
        val to = TestController("2")
        val transaction = createControllerTransaction(
            from = from,
            to = to,
            backward = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionCanceled()

        assertTrue(from.attached)
        assertTrue(onAttachControllerListener.invoked)
    }

    @Test
    fun `WHEN transaction lifecycle events triggered THEN pass events to controller container`() {
        val container = controllerManager.controllerViewHolder.container as ControllerContainer
        val transaction = createControllerTransaction(
            from = TestController("1"),
            to = TestController("2"),
            indefinite = true,
        )
        controllerManager.onAttach()

        transaction.onTransitionStart()
        transaction.onTransitionCanceled()

        container.inOrder {
            verify(container).onControllersTransitionStart(true)
            verify(container).onControllersTransitionCanceled(true)
        }
        clearInvocations(container)

        transaction.onTransitionStart()
        transaction.onTransitionEnd()

        container.inOrder {
            verify(container).onControllersTransitionStart(true)
            verify(container).onControllersTransitionEnd(true)
        }
    }

    private fun createControllerTransaction(
        from: Controller? = null,
        to: Controller? = null,
        controllerManager: ControllerManager = this.controllerManager,
        backward: Boolean = false,
        indefinite: Boolean = false,
    ) = ControllerTransaction(
        from = from,
        to = to,
        controllerManager = controllerManager,
        backward = backward,
        indefinite = indefinite,
    )

    private class FakeChildControllerListener : ChildControllerListener {
        var invoked = false
        override fun invoke(p1: Controller, p2: ControllerManager) {
            invoked = true
        }
    }
}