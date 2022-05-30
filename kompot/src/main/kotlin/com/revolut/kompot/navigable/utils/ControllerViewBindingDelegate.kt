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