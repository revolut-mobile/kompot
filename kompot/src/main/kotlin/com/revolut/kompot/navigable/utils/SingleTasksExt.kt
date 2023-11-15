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

package com.revolut.kompot.navigable.utils

import com.revolut.kompot.navigable.utils.single_task.IllegalConcurrentAccessException
import com.revolut.kompot.navigable.utils.single_task.SingleTasksRegistry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

@OptIn(FlowPreview::class)
internal fun <T> kotlinx.coroutines.flow.Flow<T>.singleTask(singleTasksRegistry: SingleTasksRegistry, taskId: String): kotlinx.coroutines.flow.Flow<T> =
    flow {
        if (singleTasksRegistry.acquire(taskId)) {
            emit(Unit)
        } else {
            throw IllegalConcurrentAccessException()
        }
    }.flatMapConcat {
        this
    }.onCompletion { cause ->
        if (cause !is IllegalConcurrentAccessException) {
            singleTasksRegistry.release(taskId)
        }
    }.catch { e ->
        if (e !is IllegalConcurrentAccessException) {
            throw e
        }
    }

internal suspend fun <T> singleTask(singleTasksRegistry: SingleTasksRegistry, taskId: String, action: suspend () -> T): T? {
    if (!singleTasksRegistry.acquire(taskId)) {
        return null
    }

    return try {
        action.invoke()
    } finally {
        singleTasksRegistry.release(taskId)
    }
}