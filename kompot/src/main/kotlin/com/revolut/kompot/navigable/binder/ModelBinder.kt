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

import androidx.annotation.VisibleForTesting

interface ModelBinder<T> : ModelObserver<T> {
    fun bind(observer: ModelObserver<T>): Binding
    fun unbind(observer: ModelObserver<T>)
}

internal open class DefaultModelBinder<T> : ModelBinder<T> {

    @VisibleForTesting
    internal val observers = mutableListOf<ModelObserver<T>>()

    override fun bind(observer: ModelObserver<T>): Binding {
        observers.add(observer)
        return BindingImpl(this, observer)
    }

    override fun unbind(observer: ModelObserver<T>) {
        observers.remove(observer)
    }

    override fun notify(value: T) {
        observers.toList().forEach { observer ->
            observer.notify(value)
        }
    }

}

internal class StatefulModelBinder<T> : DefaultModelBinder<T>() {

    private var latestValue: T? = null

    override fun bind(observer: ModelObserver<T>): Binding {
        latestValue?.let { value -> observer.notify(value) }
        return super.bind(observer)
    }

    override fun notify(value: T) {
        latestValue = value
        super.notify(value)
    }
}

@Suppress("FunctionName")
internal fun <T> ModelBinder(): ModelBinder<T> = DefaultModelBinder()

@VisibleForTesting
@Suppress("FunctionName")
fun <T> TestModelBinder(): ModelBinder<T> = DefaultModelBinder()