package com.revolut.kompot.navigable.cache

import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey

interface ControllersCache {
    fun onControllerCreated(controller: Controller)

    fun onControllerDestroyed(controller: Controller)

    fun removeController(controllerKey: ControllerKey, finish: Boolean, doAfterRemove: () -> Unit = {})

    fun getController(controllerKey: ControllerKey): Controller?

    fun isControllerCached(controllerKey: ControllerKey): Boolean

    fun clearCache()
}