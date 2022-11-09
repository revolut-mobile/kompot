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

package com.revolut.kompot.coroutines.test

import android.annotation.SuppressLint
import com.revolut.kompot.coroutines.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherExtension : BeforeAllCallback, AfterAllCallback {

    @SuppressLint("VisibleForTests")
    override fun beforeAll(context: ExtensionContext?) {
        val testDispatcher = TestContextProvider.unconfinedDispatcher()
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.dispatcherOverride = { testDispatcher }
    }

    @SuppressLint("VisibleForTests")
    override fun afterAll(context: ExtensionContext?) {
        Dispatchers.resetMain()
        AppDispatchers.dispatcherOverride = { it() }
    }
}