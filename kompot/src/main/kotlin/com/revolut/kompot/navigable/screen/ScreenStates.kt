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

package com.revolut.kompot.navigable.screen

import android.os.Parcelable
import com.revolut.recyclerkit.delegates.ListItem
import java.io.Serializable

interface ScreenStates {
    interface Domain

    interface RetainedDomain: Parcelable

    interface UI {
        fun calculatePayload(oldState: UI): UIPayload? = null
    }

    interface UIPayload

    interface UIList : UI {
        val items: List<ListItem>
    }

    object EmptyDomain : Domain

    object EmptyUI : UI

    object EmptyUIList : UIList {
        override val items: List<ListItem> = emptyList()
    }
}

data class DomainStateProperty<T, E : DomainStatePropertyError>(val value: T, val error: E? = null, val loading: Boolean = false) : Serializable

fun <T> T.asDomainStateProperty() = DomainStateProperty<T, DomainStatePropertyError>(value = this, error = null, loading = true)

val <T : Any> DomainStateProperty<List<T>, *>.firstLoading get() = loading && value.isEmpty()