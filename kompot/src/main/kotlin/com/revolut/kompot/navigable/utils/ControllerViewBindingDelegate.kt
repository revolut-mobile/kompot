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

package com.revolut.kompot.navigable.utils

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import com.revolut.kompot.navigable.Controller
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ControllerViewBindingDelegate<in C: Controller, out T : ViewBinding>(
    controller: C,
    private val viewBindingProvider: (View) -> T
) : ReadOnlyProperty<C, T> {
    private var binding: T? = null

    init {
        controller.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                binding = null
            }
        })
    }

    override fun getValue(thisRef: C, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }
        return viewBindingProvider(thisRef.view).also { this.binding = it }
    }
}

fun <T : ViewBinding> Controller.viewBinding(viewBindingProvider: (View) -> T) =
    ControllerViewBindingDelegate(this, viewBindingProvider)