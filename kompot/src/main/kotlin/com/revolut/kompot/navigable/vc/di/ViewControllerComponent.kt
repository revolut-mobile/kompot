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

import com.revolut.kompot.di.flow.ControllerComponent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.vc.ViewController
import dagger.Binds
import dagger.BindsInstance
import dagger.multibindings.Multibinds
import javax.inject.Qualifier
import javax.inject.Scope

interface ViewControllerComponent : ControllerComponent, ViewControllerExtensionsInjector,
    ViewControllerModelExtensionsInjector {
    interface Builder<T : ViewControllerComponent, B> {
        @BindsInstance
        fun controller(@ViewControllerQualifier viewController: ViewController<*>): B
        fun build(): T
    }
}

interface ViewControllerExtensionsInjector {
    fun getControllerExtensions(): Set<ControllerExtension>
}

interface ViewControllerModelExtensionsInjector {
    fun getControllerModelExtensions(): Set<ControllerModelExtension>
}

interface ViewControllerModule {

    @[Binds ViewControllerScope ViewControllerQualifier]
    fun provideController(@ViewControllerQualifier viewController: ViewController<*>): Controller

    @[Multibinds ViewControllerScope]
    fun provideControllerExtensions(): Set<ControllerExtension>

    @[Multibinds ViewControllerScope]
    fun provideControllerModelExtensions(): Set<ControllerModelExtension>
}


@Scope
annotation class ViewControllerScope

@Qualifier
annotation class ViewControllerQualifier