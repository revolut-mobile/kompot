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

package com.revolut.kompot.navigable.cache

import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey

interface ControllersCache {
    fun onControllerCreated(controller: Controller)

    fun onControllerDestroyed(controller: Controller)

    fun removeController(controllerKey: ControllerKey, finish: Boolean, doAfterRemove: () -> Unit = {})

    fun getController(controllerKey: ControllerKey): Controller?

    fun isControllerCached(controllerKey: ControllerKey): Boolean

    fun clearCache()

    fun getCacheLogWithKeys() : String
}