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

package com.revolut.kompot.di.flow

import com.revolut.kompot.di.scope.FlowQualifier
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.flow.BaseFlow
import dagger.Binds
import dagger.multibindings.Multibinds

interface BaseFlowModule {
    @Binds
    @FlowScope
    @FlowQualifier
    fun provideController(flow: BaseFlow<*, *, *>): Controller

    @Multibinds
    @FlowScope
    @FlowQualifier
    fun provideControllerExtensions(): Set<ControllerExtension>

    @Multibinds
    @FlowScope
    @FlowQualifier
    fun provideControllerModelExtensions(): Set<ControllerModelExtension>
}