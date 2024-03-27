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

package com.revolut.kompot.navigable.flow

import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.holder.ControllerViewHolder
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.transition.TransitionListener
import com.revolut.kompot.view.ControllerContainerFrameLayout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ControllerManagersHolderTest {

    private val holder = ControllerManagersHolder()

    @Test
    fun `WHEN add manager THEN add manager to the list`() {
        val manager = createControllerManager(modal = false)
        holder.add(manager, id = "1")

        assertEquals(listOf(manager), holder.all)
        assertEquals(listOf(manager), holder.allNonModal)
    }

    @Test
    fun `GIVEN modal manager WHEN add manager THEN add to the end of the list`() {
        val manager = createControllerManager(modal = false)
        val modalManager = createControllerManager(modal = true)
        holder.add(manager, id = "1")
        holder.add(modalManager, id = "2")

        assertEquals(listOf(manager, modalManager), holder.all)
        assertEquals(listOf(manager), holder.allNonModal)
    }

    @Test
    fun `GIVEN holder with modal manager WHEN add manager THEN add manager before modal`() {
        val manager = createControllerManager(modal = false)
        val modalManager = createControllerManager(modal = true)
        holder.add(modalManager, id = "1")
        holder.add(manager, id = "2")

        assertEquals(listOf(manager, modalManager), holder.all)
        assertEquals(listOf(manager), holder.allNonModal)
    }

    @Test
    fun `GIVEN empty holder WHEN gerOrAdd THEN add and return manager`() {
        val manager = createControllerManager(modal = false)
        val resultManager = holder.getOrAdd("1") { manager }

        assertEquals(resultManager, manager)
        assertEquals(listOf(manager), holder.all)
        assertEquals(listOf(manager), holder.allNonModal)
    }

    @Test
    fun `GIVEN manager added WHEN gerOrAdd THEN return existing manager`() {
        val manager = createControllerManager(modal = false)
        holder.add(manager, "1")
        val resultManager = holder.getOrAdd("1") {
            createControllerManager(modal = false)
        }

        assertEquals(resultManager, manager)
        assertEquals(listOf(manager), holder.all)
        assertEquals(listOf(manager), holder.allNonModal)
    }

    private fun createControllerManager(
        modal: Boolean,
        containerId: String = "containerId",
    ) = ControllerManager(
        modal = modal,
        defaultControllerContainer = null,
        controllersCache = DefaultControllersCache(0),
        controllerViewHolder = TestControllerViewHolder(containerId),
        onTransitionCanceled = {},
    )

    private class TestControllerViewHolder(private val containerId: String) : ControllerViewHolder {
        override val container: ViewGroup = mock<ControllerContainerFrameLayout> {
            on { this.containerId } doReturn containerId
        }

        override fun add(view: View) = Unit
        override fun addToBottom(view: View) = Unit
        override fun makeTransition(from: View?, to: View?, animation: TransitionAnimation, backward: Boolean, transitionListener: TransitionListener) = Unit
        override fun remove(view: View) = Unit
        override fun setOnDismissListener(onDismiss: () -> Unit) = Unit
    }

}