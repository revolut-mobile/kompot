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

package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.cache.ControllersCache

internal class FakeControllersCache : ControllersCache {

    override fun onControllerCreated(controller: Controller) = Unit

    override fun onControllerDestroyed(controller: Controller) = Unit

    override fun removeController(controllerKey: ControllerKey, finish: Boolean, doAfterRemove: () -> Unit) = Unit

    override fun getController(controllerKey: ControllerKey): Controller? = null

    override fun isControllerCached(controllerKey: ControllerKey): Boolean = false

    override fun clearCache() = Unit
    override fun getCacheLogWithKeys(): String = ""
}