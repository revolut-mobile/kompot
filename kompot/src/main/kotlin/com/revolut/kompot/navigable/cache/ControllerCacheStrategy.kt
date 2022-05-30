package com.revolut.kompot.navigable.cache

import com.revolut.kompot.navigable.ControllerKey

sealed class ControllerCacheStrategy {
    object Ignored : ControllerCacheStrategy()

    object Auto : ControllerCacheStrategy()

    object Prioritized : ControllerCacheStrategy()

    data class DependentOn(val key: ControllerKey) : ControllerCacheStrategy()
}