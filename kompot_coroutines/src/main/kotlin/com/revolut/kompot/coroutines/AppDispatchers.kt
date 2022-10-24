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

package com.revolut.kompot.coroutines

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object AppDispatchers {

    val Default
        get() = dispatcherOverride {
            Dispatchers.Default
        }

    val IO
        get() = dispatcherOverride {
            Dispatchers.IO
        }

    val Unconfined
        get() = dispatcherOverride {
            Dispatchers.Unconfined
        }

    /**
     * Main is testable by default. We can override it with Dispatchers.setMain(context) call
     */
    internal val Main get() = Dispatchers.Main

    @VisibleForTesting
    var dispatcherOverride: (() -> CoroutineContext) -> CoroutineContext = { it() }
}