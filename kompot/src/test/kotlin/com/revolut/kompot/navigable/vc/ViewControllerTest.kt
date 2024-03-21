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

package com.revolut.kompot.navigable.vc

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.ModalTransitionAnimation
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.components.TestControllerView
import com.revolut.kompot.navigable.components.TestFlowViewController
import com.revolut.kompot.navigable.components.TestFlowViewControllerModel
import com.revolut.kompot.navigable.components.TestViewController
import com.revolut.kompot.navigable.hooks.ControllerHook
import com.revolut.kompot.navigable.hooks.ControllerViewContextHook
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.vc.ViewController.Companion.CONTAINER_VIEW_STATE_KEY
import com.revolut.kompot.utils.StubMainThreadRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
internal class ViewControllerTest {

    @[Rule JvmField]
    val stubMainThreadRule = StubMainThreadRule()

    @Test
    fun `GIVEN view context hook WHEN getViewInflater THEN apply context from hook`() {
        val controller = TestViewController("")
        val controllerManager = ControllerManager(
            defaultControllerContainer = 1,
            controllersCache = DefaultControllersCache(20),
            controllerViewHolder = mock(),
            modal = false,
        ).apply {
            hooksProvider = TestViewCtxHookProvider()
        }

        controller.bind(controllerManager, null)

        val baseCtx = ApplicationProvider.getApplicationContext<Context>()
        val actualInflater = controller.getViewInflater(LayoutInflater.from(baseCtx))
        val expectedCtx = TestViewCtx(baseCtx)

        assertEquals(expectedCtx, actualInflater.context)
    }

    @Test
    fun `GIVEN controller theme id WHEN getViewInflater THEN themed context applied to inflater`() {
        val baseInflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())

        val actualInflater = TestViewController("", themeId = 42).getViewInflater(baseInflater)
        assertTrue(actualInflater.context is ContextThemeWrapper)
    }

    @Test
    fun `GIVEN view context hook, theme id WHEN getViewInflater THEN theme id and context hook applied to base context`() {
        val controller = TestViewController("", themeId = 42)
        val controllerManager = ControllerManager(
            defaultControllerContainer = 1,
            controllersCache = DefaultControllersCache(20),
            controllerViewHolder = mock(),
            modal = false,
        ).apply {
            hooksProvider = TestViewCtxHookProvider()
        }

        controller.bind(controllerManager, null)

        val baseCtx = ApplicationProvider.getApplicationContext<Context>()
        val actualInflater = controller.getViewInflater(LayoutInflater.from(baseCtx))
        val actualContext = actualInflater.context
        val actualIntermediateContext = (actualContext as? ContextThemeWrapper)?.baseContext

        assertTrue(actualContext is ContextThemeWrapper)
        assertEquals(actualIntermediateContext, TestViewCtx(baseCtx))
    }

    @Test
    fun `GIVEN no hooks, no theme id WHEN getViewInflater THEN return base inflater`() {
        val baseInflater = LayoutInflater.from(ApplicationProvider.getApplicationContext())

        val actualInflater = TestViewController("").getViewInflater(baseInflater)
        assertEquals(baseInflater, actualInflater)
    }

    @Test
    fun `GIVEN bound popup enter transition WHEN request modal style THEN return popup style`() {
        val controller = TestViewController("").apply {
            bind(mock(), mock(), ModalTransitionAnimation.ModalPopup())
        }

        assertEquals(ModalDestination.Style.POPUP, controller.environment.modalStyle)
    }

    @Test
    fun `GIVEN bound default transition WHEN request modal style THEN return null`() {
        val controller = TestViewController("").apply {
            bind(mock(), mock(), TransitionAnimation.SLIDE_LEFT_TO_RIGHT)
        }

        assertNull(controller.environment.modalStyle)
    }

    @Test
    fun `GIVEN saved state WHEN restore from saved state THEN restore view state`() {
        val viewMarker = 42
        val controller = TestViewController("", viewMarker = viewMarker)

        val bundle = Bundle()
        controller.onCreate()
        controller.onAttach()
        controller.saveState(bundle)

        val restoredController = TestViewController("")
        restoredController.restoreState(bundle)
        restoredController.onCreate()
        restoredController.onAttach()

        val restoredView = restoredController.view as TestControllerView
        assertEquals(viewMarker, restoredView.restoredMarker)
    }

    @Test
    fun `GIVEN view saved state disabled WHEN restore from saved state THEN don't restore view state`() {
        val controller = TestViewController("", viewSavedStateEnabled = false, viewMarker = 42)

        val bundle = Bundle()
        controller.onCreate()
        controller.onAttach()
        controller.saveState(bundle)

        val restoredController = TestViewController("")
        restoredController.restoreState(bundle)
        restoredController.onCreate()
        restoredController.onAttach()

        val restoredView = restoredController.view as TestControllerView
        assertNull(restoredView.restoredMarker)
    }

    @Test
    fun `GIVEN flow saved state WHEN save state THEN store flow view state only in flow state`() {
        val flowModel = TestFlowViewControllerModel()
        val flow = TestFlowViewController(flowModel)

        val bundle = Bundle()
        flow.onCreate()
        flow.onAttach()
        flow.saveState(bundle)

        val flowContainerState = bundle.getSparseParcelableArray<Parcelable>(CONTAINER_VIEW_STATE_KEY)
        assertEquals(1, flowContainerState!!.size())
    }

    @Test
    fun `GIVEN flow saved state WHEN restore from saved state THEN restore child controller view`() {
        val flowModel = TestFlowViewControllerModel()
        val flow = TestFlowViewController(flowModel)

        val bundle = Bundle()
        flow.onCreate()
        flow.onAttach()
        flow.saveState(bundle)

        val restoredFlowModel = TestFlowViewControllerModel()
        val restoredFlow = TestFlowViewController(restoredFlowModel)

        restoredFlow.restoreState(bundle)
        restoredFlow.onCreate()
        restoredFlow.onAttach()

        val flowChild = restoredFlow.currentController as TestViewController
        val flowChildView = flowChild.view as TestControllerView
        assertNotNull(flowChildView.restoredMarker)
    }

    @Test
    fun `GIVEN controller restored from saved state WHEN check model restored THEN model restored`() {
        val controller = TestViewController("")

        val bundle = Bundle()
        controller.onCreate()
        controller.onAttach()
        controller.saveState(bundle)

        val restoredController = TestViewController("")
        restoredController.restoreState(bundle)
        restoredController.onCreate()
        restoredController.onAttach()

        assertTrue(restoredController.model._restored)
    }

    @Test
    fun `GIVEN controller not restored from saved state WHEN check model restored THEN model not restored`() {
        val controller = TestViewController("")

        controller.onCreate()
        controller.onAttach()

        assertFalse(controller.model._restored)
    }

    private class TestViewCtxHookProvider : HooksProvider {
        override fun <T : ControllerHook> getHook(key: ControllerHook.Key<T>): T =
            ControllerViewContextHook { _, ctx ->
                TestViewCtx(ctx)
            } as T
    }

    private data class TestViewCtx(val baseCtx: Context) : ContextThemeWrapper(baseCtx, 42)
}