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

package com.revolut.kompot.sample.test.core

import android.annotation.SuppressLint
import com.revolut.kompot.coroutines.test.TestDispatchersHolder
import com.revolut.kompot.sample.utils.AppDispatchersPlugins
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherExtension : BeforeAllCallback, AfterAllCallback {

    val defaultDispatcher = TestDispatchersHolder.unconfinedTestDispatcher
    val ioDispatcher = TestDispatchersHolder.unconfinedTestDispatcher

    @SuppressLint("VisibleForTests")
    override fun beforeAll(context: ExtensionContext?) {
        AppDispatchersPlugins.setDefault(defaultDispatcher)
        AppDispatchersPlugins.setIo(ioDispatcher)
    }

    @SuppressLint("VisibleForTests")
    override fun afterAll(context: ExtensionContext?) {
        AppDispatchersPlugins.reset()
    }
}