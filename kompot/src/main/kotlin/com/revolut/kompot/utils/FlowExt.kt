package com.revolut.kompot.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("FunctionName")
fun ControllerScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

internal fun <T> Flow<T>.debounceButEmitFirst(timeoutMillis: Long): Flow<T> =
    debounce(object : (T) -> Long {
        val firstEmission = AtomicBoolean(true)
        override fun invoke(item: T): Long =
            if (firstEmission.getAndSet(false)) 0 else timeoutMillis
    })

internal fun <T> Flow<T>.withPrevious(): Flow<Pair<T?, T>> = flow {
    var prev: T? = null
    collect { value ->
        emit(prev to value)
        prev = value
    }
}

@Suppress("FunctionName")
internal fun <T> MutableBehaviourFlow() = MutableSharedFlow<T>(
    extraBufferCapacity = DEFAULT_EXTRA_BUFFER_CAPACITY - 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    replay = 1,
)