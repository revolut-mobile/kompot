package com.revolut.kompot.sample.utils

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Suppress("FunctionName")
fun <T> MutableBufferedSharedFlow() = MutableSharedFlow<T>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_LATEST
)

fun <T> Flow<T>.onIo(): Flow<T> = this.flowOn(AppDispatchers.IO)

suspend fun <T> onIo(block: suspend CoroutineScope.() -> T) = withContext(AppDispatchers.IO, block)

object AppDispatchers {

    internal var io = Dispatchers.IO
    val IO get() = io

    internal var default = Dispatchers.Default
    val Default get() = default

    val Main get() = Dispatchers.Main

    val Unconfined get() = Dispatchers.Unconfined

}

object AppDispatchersPlugins {

    @VisibleForTesting
    fun setIo(dispatcher: CoroutineDispatcher) {
        AppDispatchers.io = dispatcher
    }

    @VisibleForTesting
    fun setDefault(dispatcher: CoroutineDispatcher) {
        AppDispatchers.default = dispatcher
    }

    @VisibleForTesting
    fun reset() {
        AppDispatchers.io = Dispatchers.IO
        AppDispatchers.default = Dispatchers.Default
    }

}