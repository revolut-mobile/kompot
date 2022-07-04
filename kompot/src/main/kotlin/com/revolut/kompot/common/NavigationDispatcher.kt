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

package com.revolut.kompot.common

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.screen.Screen
import kotlinx.parcelize.RawValue

interface NavigationDestination

data class NavigationEvent(val destination: NavigationDestination) : Event()

fun EventsDispatcher.handleNavigationEvent(destination: NavigationDestination): EventResult? = handleEvent(NavigationEvent(destination))

object NavigationEventHandledResult : EventResult

open class InternalDestination<INPUT : IOData.Input>(open val inputData: INPUT) : NavigationDestination {
    open val addCurrentStepToBackStack: Boolean = true
}

sealed class ModalDestination : NavigationDestination {
    data class ExplicitScreen<T : IOData.Output>(
        val screen: Screen<T>,
        val style: Style = Style.FULLSCREEN,
        val onResult: ((T) -> Unit)? = null
    ) : ModalDestination()

    data class ExplicitFlow<T : IOData.Output>(
        val flow: Flow<T>,
        val style: Style = Style.FULLSCREEN,
        val onResult: ((T) -> Unit)? = null
    ) : ModalDestination()

    enum class Style {
        FULLSCREEN, POPUP
    }
}

sealed class ExternalDestination : NavigationDestination {
    abstract val requestCode: Int?

    data class Browser(
        val url: String,
        override val requestCode: Int? = null
    ) : ExternalDestination()

    data class ByIntent(
        val intent: Intent,
        override val requestCode: Int? = null
    ) : ExternalDestination()

    data class ExplicitActivity(
        val clazz: Class<out Activity>,
        val extras: Map<String, @RawValue Any?> = emptyMap(),
        val flags: Int = 0,
        val type: String? = null,
        override val requestCode: Int? = null
    ) : ExternalDestination()

    data class ImplicitActivity(
        val action: String,
        val extras: Map<String, @RawValue Any?> = emptyMap(),
        val packageName: String? = null,
        val className: String? = null,
        val uri: Uri? = null,
        val flags: Int = 0,
        val type: String? = null,
        val component: ComponentName? = null,
        override val requestCode: Int? = null
    ) : ExternalDestination()
}