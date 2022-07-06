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

package com.revolut.kompot.navigable.binder

interface Binding {
    fun clear()
}

internal class BindingImpl(
    private val modelBinder: ModelBinder<*>,
    private val modelObserver: ModelObserver<*>
): Binding {

    @Suppress("UNCHECKED_CAST")
    override fun clear() {
        modelBinder.unbind(modelObserver as ModelObserver<Any?>)
    }

}

internal class CompositeBinding: Binding {

    private val bindings = mutableListOf<Binding>()

    operator fun plusAssign(binding: Binding) {
        bindings.add(binding)
    }

    override fun clear() {
        bindings.forEach { it.clear() }
        bindings.clear()
    }

}