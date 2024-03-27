/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.utils

import com.revolut.kompot.coroutines.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("FunctionName")
fun ControllerScope(context: CoroutineContext = EmptyCoroutineContext): CoroutineScope = AppCoroutineScope(Dispatchers.Main.immediate + context)

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