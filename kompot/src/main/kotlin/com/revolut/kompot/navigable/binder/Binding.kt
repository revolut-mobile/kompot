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