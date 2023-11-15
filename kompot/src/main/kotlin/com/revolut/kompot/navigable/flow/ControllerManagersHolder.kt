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

import com.revolut.kompot.navigable.ControllerManager

internal class ControllerManagersHolder : ControllerManagersProvider {

    private var modalsStartInd = 0
    private val controllerManagers = mutableListOf<ControllerManagerWithId>()

    override val all: List<ControllerManager> get() = controllerManagers.map { it.controllerManager }
    val allNonModal: List<ControllerManager>
        get() = controllerManagers.mapNotNull {
            it.controllerManager.takeIf { manager -> !manager.modal }
        }

    fun add(controllerManager: ControllerManager, id: String) {
        val modal = controllerManager.modal
        if (modal) {
            controllerManagers.add(ControllerManagerWithId(id, controllerManager))
        } else {
            val index = modalsStartInd++
            controllerManagers.add(index, ControllerManagerWithId(id, controllerManager))
        }
    }

    inline fun getOrAdd(id: String, defaultValue: () -> ControllerManager): ControllerManager {
        get(id)?.let { return it }
        return defaultValue().also { controllerManager ->
            add(controllerManager, id)
        }
    }

    fun get(id: String): ControllerManager? = controllerManagers.find { it.id == id }?.controllerManager

    private class ControllerManagerWithId(
        val id: String,
        val controllerManager: ControllerManager,
    )
}

internal interface ControllerManagersProvider {
    val all: List<ControllerManager>
}