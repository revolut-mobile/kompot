package com.revolut.kompot.navigable.binder

import androidx.annotation.VisibleForTesting

interface ModelBinder<T>: ModelObserver<T> {
    fun bind(observer: ModelObserver<T>): Binding
    fun unbind(observer: ModelObserver<T>)
}

internal class DefaultModelBinder<T> : ModelBinder<T> {

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

@Suppress("FunctionName")
internal fun <T> ModelBinder(): ModelBinder<T> = DefaultModelBinder()

@VisibleForTesting
@Suppress("FunctionName")
fun <T> TestModelBinder(): ModelBinder<T> = DefaultModelBinder()