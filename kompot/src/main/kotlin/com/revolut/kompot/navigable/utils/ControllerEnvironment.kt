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

package com.revolut.kompot.navigable.utils

import com.revolut.kompot.R
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.extractModalStyle

class ControllerEnvironment(private val controller: Controller) {

    internal var enterTransition: TransitionAnimation? = null
    /**
     * @return modal style if controller was started as modal. Null otherwise
     */
    val modalStyle: ModalDestination.Style? get() = enterTransition?.extractModalStyle()

    val defaultControllerContainer: Int
        get() =
            controller.parentControllerManager.defaultControllerContainer ?: R.layout.base_flow_container

    fun isModalRoot(): Boolean {
        var result = false

        var currentController = controller
        var parent = currentController.parentController

        while (parent != null) {
            if (currentController.parentControllerManager.modal) {
                result = true
                break
            }
            val parentFlow = parent as? ParentFlow
            if (parentFlow == null || parentFlow.hasBackStack) {
                break
            }
            currentController = parent
            parent = parent.parentController
        }

        return result
    }
}