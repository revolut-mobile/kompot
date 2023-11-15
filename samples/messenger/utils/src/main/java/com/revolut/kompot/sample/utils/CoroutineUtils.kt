package com.revolut.kompot.sample.utils

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

fun <T> Flow<T>.onIo(): Flow<T> = this.flowOn(Dispatchers.IO)

suspend fun <T> onIo(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO, block)