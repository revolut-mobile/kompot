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

package com.revolut.kompot.navigable.vc.di

import com.revolut.kompot.di.scope.FlowQualifier
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.vc.ViewController
import dagger.Binds
import dagger.BindsInstance
import dagger.multibindings.Multibinds

interface FlowViewControllerComponent : ViewControllerComponent {

    @FlowQualifier
    override fun getControllerModelExtensions(): Set<ControllerModelExtension>

    @FlowQualifier
    override fun getControllerExtensions(): Set<ControllerExtension>

    interface Builder<T : FlowViewControllerComponent, B> {
        @BindsInstance
        fun controller(viewController: ViewController<*>): B
        fun build(): T
    }
}

interface FlowViewControllerModule {

    @[Binds FlowQualifier FlowScope]
    fun provideController(controller: ViewController<*>): Controller

    @[Multibinds FlowQualifier FlowScope]
    fun provideControllerExtensions(): Set<ControllerExtension>

    @[Multibinds FlowQualifier FlowScope]
    fun provideControllerModelExtensions(): Set<ControllerModelExtension>
}