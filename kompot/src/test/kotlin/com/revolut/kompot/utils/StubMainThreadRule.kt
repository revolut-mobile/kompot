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

package com.revolut.kompot.utils

import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
class StubMainThreadRule : TestRule {

    override fun apply(base: Statement, d: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                Preconditions.mainThreadRequirementEnabled = false
                Dispatchers.setMain(UnconfinedTestDispatcher())
                try {
                    base.evaluate()
                } finally {
                    Preconditions.mainThreadRequirementEnabled = true
                    Dispatchers.resetMain()
                }
            }
        }
    }
}