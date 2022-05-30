package com.revolut.kompot.common

import com.revolut.kompot.navigable.Controller

interface EventsDispatcher {
    fun handleEvent(event: Event): EventResult?
}

abstract class Event {
    internal var _controller: Controller? = null
    val controller: Controller
        get() = _controller!!
}

interface EventResult