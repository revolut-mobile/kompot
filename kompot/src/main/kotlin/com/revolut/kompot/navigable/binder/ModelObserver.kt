package com.revolut.kompot.navigable.binder

fun interface ModelObserver<in T> {
    fun notify(value: T)
}