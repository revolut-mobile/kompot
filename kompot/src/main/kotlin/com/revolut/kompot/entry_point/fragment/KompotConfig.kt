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

package com.revolut.kompot.entry_point.fragment

import androidx.annotation.LayoutRes
import com.revolut.kompot.R
import com.revolut.kompot.entry_point.KompotDelegate
import com.revolut.kompot.navigable.root.RootFlow

data class KompotConfig(
    val rootFlow: RootFlow<*, *>,
    @LayoutRes val defaultControllerContainer: Int? = R.layout.base_flow_container,
    val trimCacheThreshold: Int = DEFAULT_TRIM_CACHE_THRESHOLD,
    val savedStateEnabled: Boolean = true,
    val fullScreenEnabled: Boolean = true,
) {

    companion object {
        const val DEFAULT_TRIM_CACHE_THRESHOLD = 30
    }
}

internal fun KompotConfig.createDelegate() = KompotDelegate(
    rootFlow = rootFlow,
    defaultControllerContainer = defaultControllerContainer,
    trimCacheThreshold = trimCacheThreshold,
    savedStateEnabled = savedStateEnabled,
    fullScreenEnabled = fullScreenEnabled,
)