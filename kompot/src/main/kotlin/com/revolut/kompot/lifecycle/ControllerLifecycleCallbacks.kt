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

package com.revolut.kompot.lifecycle

import com.revolut.kompot.navigable.Controller

interface ControllerLifecycleCallbacks {

    fun onControllerCreated(controller: Controller) = Unit
    fun onControllerAttached(controller: Controller) = Unit
    fun onControllerDetached(controller: Controller) = Unit
    fun onControllerDestroyed(controller: Controller) = Unit

    fun onTransitionStart(controller: Controller, enter: Boolean) = Unit
    fun onTransitionEnd(controller: Controller, enter: Boolean) = Unit
}