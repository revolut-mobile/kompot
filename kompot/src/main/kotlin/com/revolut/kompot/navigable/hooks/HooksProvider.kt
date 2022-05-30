package com.revolut.kompot.navigable.hooks

internal interface HooksProvider {
    fun <T : ControllerHook> getHook(key: ControllerHook.Key<T>): T?
}