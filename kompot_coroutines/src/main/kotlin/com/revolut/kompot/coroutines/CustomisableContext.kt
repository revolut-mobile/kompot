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

package com.revolut.kompot.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CustomContextWrapper(context: CoroutineContext) : CoroutineContext {

    private val wrappedContext = context.withTestableContinuationInterceptor()

    override fun plus(context: CoroutineContext): CoroutineContext =
        CustomContextWrapper(wrappedContext.plus(context.withTestableContinuationInterceptor()))

    override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R =
        wrappedContext.fold(initial, operation)

    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
        wrappedContext[key]

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext =
        wrappedContext.minusKey(key)
}

private fun CoroutineContext.withTestableContinuationInterceptor(): CoroutineContext {
    val testableInterceptor = when (val interceptor = this[ContinuationInterceptor]) {
        Dispatchers.IO -> AppDispatchers.IO
        Dispatchers.Default -> AppDispatchers.Default
        Dispatchers.Unconfined -> AppDispatchers.Unconfined
        else -> interceptor ?: EmptyCoroutineContext
    }
    return this + testableInterceptor
}

@Suppress("FunctionName")
fun AppCoroutineScope(context: CoroutineContext = Dispatchers.Default) =
    CoroutineScope(CustomContextWrapper(SupervisorJob() + context))