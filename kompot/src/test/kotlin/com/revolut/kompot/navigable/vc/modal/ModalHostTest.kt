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

package com.revolut.kompot.navigable.vc.modal

import android.os.Build
import android.os.Bundle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.navigable.components.TestFlowViewController
import com.revolut.kompot.navigable.components.TestFlowViewControllerModel
import com.revolut.kompot.navigable.components.TestRootFlow
import com.revolut.kompot.navigable.components.TestStep
import com.revolut.kompot.navigable.components.TestViewController
import com.revolut.kompot.navigable.root.RootNavigator
import com.revolut.kompot.utils.StubMainThreadRule
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
class ModalHostTest {

    @[Rule JvmField]
    val stubMainThreadRule = StubMainThreadRule()

    private val modalHostModel = TestFlowViewControllerModel()
    private val modalHost = TestFlowViewController(modalHostModel)
    private val rootFlow = TestRootFlow(modalHost)

    @Test
    fun `WHEN modal called THEN root flow opens a modal`() {
        rootFlow.onCreate()
        modalHost.onAttach()
        rootFlow.onAttach()

        modalHostModel.flowCoordinator.openModal(TestStep(2), style = ModalDestination.Style.POPUP)

        rootFlow.model.rootNavigator.assertModalControllersOpened(listOf("2"))
    }

    @Test
    fun `GIVEN multiple opened modals WHEN state restored THEN restore modals in-order`() {
        rootFlow.onCreate()
        modalHost.onAttach()
        rootFlow.onAttach()
        rootFlow.model.onControllersFirstLayout()

        modalHostModel.flowCoordinator.openModal(TestStep(3), style = ModalDestination.Style.POPUP)
        modalHostModel.flowCoordinator.openModal(TestStep(2), style = ModalDestination.Style.POPUP)
        rootFlow.model.rootNavigator.triggerLatestModalsCreation(count = 2)

        val bundle = Bundle()
        rootFlow.saveState(bundle)

        val restoredModalHostModel = TestFlowViewControllerModel()
        val restoredModalHost = TestFlowViewController(restoredModalHostModel)
        val restoredRootFlow = TestRootFlow(restoredModalHost)

        restoredRootFlow.doOnCreate { restoredRootFlow.restoreState(bundle) }

        restoredRootFlow.onCreate()
        restoredModalHost.onAttach()
        restoredRootFlow.onAttach()
        restoredRootFlow.model.onControllersFirstLayout()

        restoredRootFlow.model.rootNavigator.assertModalControllersOpened(listOf("3", "2"), shownImmediately = true)
    }

    @Test
    fun `GIVEN modals opened and then destroyed WHEN state restored THEN don't trigger modals`() {
        rootFlow.onCreate()
        modalHost.onAttach()
        rootFlow.onAttach()
        rootFlow.model.onControllersFirstLayout()

        modalHostModel.flowCoordinator.openModal(TestStep(3), style = ModalDestination.Style.POPUP)
        modalHostModel.flowCoordinator.openModal(TestStep(2), style = ModalDestination.Style.POPUP)
        rootFlow.model.rootNavigator.triggerLatestModalsCreation(count = 2)
        rootFlow.model.rootNavigator.triggerLatestModalsDestruction(count = 2)

        val bundle = Bundle()
        rootFlow.saveState(bundle)

        val restoredModalHostModel = TestFlowViewControllerModel()
        val restoredModalHost = TestFlowViewController(restoredModalHostModel)
        val restoredRootFlow = TestRootFlow(restoredModalHost)

        restoredRootFlow.doOnCreate { restoredRootFlow.restoreState(bundle) }

        restoredRootFlow.onCreate()
        restoredModalHost.onAttach()
        restoredRootFlow.onAttach()
        restoredRootFlow.model.onControllersFirstLayout()

        restoredRootFlow.model.rootNavigator.assertModalControllersOpened(emptyList(), shownImmediately = true)
    }

    @Test
    fun `GIVEN opened modals WHEN postponed state restored after root attached THEN restore modals`() {
        rootFlow.onCreate()
        modalHost.onAttach()
        rootFlow.onAttach()
        rootFlow.model.onControllersFirstLayout()

        modalHostModel.flowCoordinator.openModal(TestStep(2), style = ModalDestination.Style.POPUP)
        modalHostModel.flowCoordinator.openModal(TestStep(3), style = ModalDestination.Style.POPUP)
        rootFlow.model.rootNavigator.triggerLatestModalsCreation(count = 2)

        val bundle = Bundle()
        rootFlow.saveState(bundle)

        val restoredModalHostModel = TestFlowViewControllerModel()
        val restoredModalHost = TestFlowViewController(restoredModalHostModel)
        val restoredRootFlow = TestRootFlow(TestViewController(""), postponeStateRestore = true)

        restoredRootFlow.doOnCreate { restoredRootFlow.restoreState(bundle) }

        restoredRootFlow.onCreate()
        restoredRootFlow.onAttach()
        restoredRootFlow.model.onControllersFirstLayout()

        restoredRootFlow.model.child = restoredModalHost
        restoredRootFlow.model.startPostponedSavedStateRestore()

        restoredRootFlow.model.rootNavigator.assertModalControllersOpened(listOf("2", "3"), shownImmediately = true)
    }

    @Test
    fun `GIVEN opened modals WHEN postponed state restored before root attached THEN restore modals`() {
        rootFlow.onCreate()
        modalHost.onAttach()
        rootFlow.onAttach()
        rootFlow.model.onControllersFirstLayout()

        modalHostModel.flowCoordinator.openModal(TestStep(2), style = ModalDestination.Style.POPUP)
        modalHostModel.flowCoordinator.openModal(TestStep(3), style = ModalDestination.Style.POPUP)
        rootFlow.model.rootNavigator.triggerLatestModalsCreation(count = 2)

        val bundle = Bundle()
        rootFlow.saveState(bundle)

        val restoredModalHostModel = TestFlowViewControllerModel()
        val restoredModalHost = TestFlowViewController(restoredModalHostModel)
        val restoredRootFlow = TestRootFlow(TestViewController(""), postponeStateRestore = true)

        restoredRootFlow.doOnCreate { restoredRootFlow.restoreState(bundle) }

        restoredRootFlow.onCreate()
        restoredRootFlow.model.child = restoredModalHost
        restoredRootFlow.model.startPostponedSavedStateRestore()
        restoredRootFlow.onAttach()
        restoredRootFlow.model.onControllersFirstLayout()

        restoredRootFlow.model.rootNavigator.assertModalControllersOpened(listOf("2", "3"), shownImmediately = true)
    }

    private fun RootNavigator.assertModalControllersOpened(descriptors: List<String>, shownImmediately: Boolean = false) {
        val modalCommandsCaptor = argumentCaptor<ModalDestination.CallbackController>()
        verify(this, times(descriptors.size)).openModal(modalCommandsCaptor.capture(), callerController = any(), showImmediately = eq(shownImmediately))
        val actualDescriptors = modalCommandsCaptor.allValues.map {
            (it.controller as TestViewController).input
        }
        assertEquals(descriptors, actualDescriptors)
    }

    private fun RootNavigator.triggerLatestModalsCreation(count: Int) {
        val modalCommandCaptor = argumentCaptor<ModalDestination.CallbackController>()
        verify(this, times(count)).openModal(modalCommandCaptor.capture(), callerController = any(), showImmediately = any())
        modalCommandCaptor.allValues.forEach {
            val openedController = it.controller as TestViewController
            openedController.onCreate()
        }
    }

    private fun RootNavigator.triggerLatestModalsDestruction(count: Int) {
        val modalCommandCaptor = argumentCaptor<ModalDestination.CallbackController>()
        verify(this, times(count)).openModal(modalCommandCaptor.capture(), callerController = any(), showImmediately = any())
        modalCommandCaptor.allValues.forEach {
            val openedController = it.controller as TestViewController
            openedController.onDestroy()
        }
    }
}
