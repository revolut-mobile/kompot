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

package com.revolut.kompot.navigable.root

import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.toModalTransitionAnimation
import com.revolut.kompot.view.ControllerContainer
import com.revolut.kompot.view.ControllerContainerFrameLayout

abstract class RootFlow<STEP : FlowStep, INPUT_DATA : IOData.Input>(inputData: INPUT_DATA) : BaseFlow<STEP, INPUT_DATA, IOData.EmptyOutput>(inputData) {
    abstract val rootDialogDisplayer: DialogDisplayer

    abstract val containerForModalNavigation: ControllerContainerFrameLayout

    private var modalManagersCount = 0

    override var cacheStrategy: ControllerCacheStrategy = ControllerCacheStrategy.Prioritized

    internal val navActionsScheduler = NavActionsScheduler()

    override fun onCreateFlowView(view: View) {
        super.onCreateFlowView(view)

        rootDialogDisplayer.onCreate()

        (flowModel as BaseRootFlowModel<*, *>).rootNavigator = RootNavigator(this)

        view.doOnLayout {
            (flowModel as BaseRootFlowModel<*, *>).onControllersFirstLayout()
        }
    }

    override fun onDestroyFlowView() {
        super.onDestroyFlowView()

        rootDialogDisplayer.onDestroy()
        navActionsScheduler.cancelAll()
    }

    override fun onAttach() {
        super.onAttach()

        rootDialogDisplayer.onAttach()
    }

    override fun onDetach() {
        super.onDetach()

        rootDialogDisplayer.onDetach()
    }

    internal fun open(controller: Controller, style: ModalDestination.Style, parentController: Controller?, showImmediately: Boolean) {
        val openModalAction = {
            containerForModalNavigation.containerId = ControllerContainer.MODAL_CONTAINER_ID
            containerForModalNavigation.isVisible = true
            getFirstAvailableModalManager().show(
                controller = controller,
                animation = if (getModalAnimatable() != null) {
                    style.toModalTransitionAnimation(showImmediately)
                } else {
                    TransitionAnimation.FADE
                },
                backward = false,
                parentController = parentController ?: this,
            )
        }
        if (showImmediately || style == ModalDestination.Style.FULLSCREEN_IMMEDIATE) {
            openModalAction()
        } else {
            navActionsScheduler.schedule(key.value, openModalAction)
        }
    }

    override fun onChildControllerAttached(controller: Controller, controllerManager: ControllerManager) {
        super.onChildControllerAttached(controller, controllerManager)
        if (controllerManager.modal) {
            modalManagersCount++
        }

        if (controllerManager.modal) {
            containerForModalNavigation.isClickable = true
        }
    }

    override fun onChildControllerDetached(controller: Controller, controllerManager: ControllerManager) {
        super.onChildControllerDetached(controller, controllerManager)
        if (controllerManager.modal) {
            modalManagersCount--
        }
        if (modalManagersCount <= 0) {
            containerForModalNavigation.isClickable = false
        }
    }

    private fun getFirstAvailableModalManager(): ControllerManager =
        getOrCreateChildControllerManager(containerForModalNavigation, "modalControllerManager_N${modalManagersCount}")

    override fun updateUi(step: STEP) = Unit

    override fun handleQuit() {
        if (!flowModel.hasBackStack) {
            handleQuitOnEmptyBackStack()
        } else {
            super.handleQuit()
        }
    }

    override fun handleFlowBack(): BackHandleResult {
        if (super.handleFlowBack() == BackHandleResult.UNHANDLED) {
            handleBackOnEmptyBackStack()
        }
        return BackHandleResult.INTERCEPTED
    }

    protected open fun handleQuitOnEmptyBackStack() {
        activity.finish()
    }

    protected open fun handleBackOnEmptyBackStack() {
        activity.onBackPressed()
    }

}