package com.revolut.kompot.navigable.screen

import com.revolut.kompot.navigable.ControllerExtension

interface ScreenExtensionsInjector {
    fun getControllerExtensions(): Set<ControllerExtension>
}