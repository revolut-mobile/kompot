package com.revolut.kompot.common

data class ErrorEvent(val throwable: Throwable) : Event()

data class ErrorEventResult(val handled: Boolean) : EventResult