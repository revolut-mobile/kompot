package com.revolut.kompot.navigable.binder

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> ModelBinder<T>.asFlow(): Flow<T> = callbackFlow {
    val binding = this@asFlow.bind { value ->
        trySendBlocking(value)
    }
    awaitClose { binding.clear() }
}