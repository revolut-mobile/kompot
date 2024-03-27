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

@file:OptIn(ExperimentalKompotApi::class)

package com.revolut.kompot.navigable.utils

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.ControllerDescriptor
import com.revolut.kompot.common.ControllerHolder
import com.revolut.kompot.common.ControllerRequest
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.InternalDestination
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationRequest
import com.revolut.kompot.common.NavigationRequestEvent
import com.revolut.kompot.common.NavigationRequestResult
import com.revolut.kompot.common.handleNavigationEvent
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController

internal fun navigate(eventsDispatcher: EventsDispatcher, internalDestination: InternalDestination<*>) = internalDestination.navigate(eventsDispatcher)

internal fun NavigationDestination.navigate(eventsDispatcher: EventsDispatcher) = eventsDispatcher.handleNavigationEvent(this)

internal fun <T : IOData.Output> Screen<T>.showModal(
    eventsDispatcher: EventsDispatcher,
    style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    onResult: ((T) -> Unit)? = null
) =
    eventsDispatcher.handleNavigationEvent(
        ModalDestination.ExplicitScreen(
            screen = this,
            onResult = onResult,
            style = style
        )
    )

internal fun <T : IOData.Output> Flow<T>.showModal(
    eventsDispatcher: EventsDispatcher,
    style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    onResult: ((T) -> Unit)? = null
) =
    eventsDispatcher.handleNavigationEvent(
        ModalDestination.ExplicitFlow(
            flow = this,
            onResult = onResult,
            style = style
        )
    )

@OptIn(ExperimentalKompotApi::class)
internal fun <T : IOData.Output> ScrollerFlow<T>.showModal(
    eventsDispatcher: EventsDispatcher,
    style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    onResult: ((T) -> Unit)? = null
) =
    eventsDispatcher.handleNavigationEvent(
        ModalDestination.ExplicitScrollerFlow(
            flow = this,
            onResult = onResult,
            style = style
        )
    )

internal fun <T : IOData.Output> ViewController<T>.showModal(
    eventsDispatcher: EventsDispatcher,
    style: ModalDestination.Style = ModalDestination.Style.FULLSCREEN_FADE,
    onResult: ((T) -> Unit)? = null
) {
    onResult?.let(this::withResult)
    eventsDispatcher.handleNavigationEvent(
        ModalDestination.CallbackController(
            controller = this,
            style = style
        )
    )
}

internal fun <T : IOData.Output> ControllerDescriptor<T>.getController(
    eventsDispatcher: EventsDispatcher
): ViewController<T> {
    val result = eventsDispatcher.handleEvent(ControllerRequest(this))
    check(result is ControllerHolder) { "Can't resolve controller for $this" }
    @Suppress("UNCHECKED_CAST") //Type safety is controlled by FeatureGateway
    return result.controller as ViewController<T>
}

internal suspend fun NavigationRequest.navigate(eventsDispatcher: EventsDispatcher) {
    val navigationRequestResult = eventsDispatcher.handleEvent(NavigationRequestEvent(this))
    val navigationRequestResolver = (navigationRequestResult as? NavigationRequestResult)?.requestResolver ?: error("Couldn't resolve request $this")
    navigationRequestResolver.invoke().navigate(eventsDispatcher = eventsDispatcher)
}