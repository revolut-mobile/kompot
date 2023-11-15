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

package com.revolut.kompot.core.test.assertion

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController
import org.junit.jupiter.api.Assertions

internal object ControllerModelAssertions {

    fun assertModalScreen(
        eventsDispatcher: EventsDispatcher,
        assertion: (Screen<*>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val screen = (firstValue.destination as ModalDestination.ExplicitScreen<*>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen! Actual value: ${firstValue.destination}\n"
            )
        }
    }

    fun assertModalFlow(
        eventsDispatcher: EventsDispatcher,
        assertion: (Flow<*>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val screen = (firstValue.destination as ModalDestination.ExplicitFlow<*>).flow
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for flow! Actual value: ${firstValue.destination}\n"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IOData.Output> assertModalScreen(
        eventsDispatcher: EventsDispatcher,
        outputToReturn: T,
        assertion: (Screen<T>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val destination = firstValue.destination
            val screen = (destination as ModalDestination.ExplicitScreen<T>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen! Actual value: ${firstValue.destination}\n"
            )

            destination.onResult?.invoke(outputToReturn)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IOData.Output> assertModalFlow(
        eventsDispatcher: EventsDispatcher,
        outputToReturn: T,
        assertion: (Flow<in T>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val destination = firstValue.destination
            val flow = (destination as ModalDestination.ExplicitFlow<T>).flow
            Assertions.assertTrue(
                assertion(flow),
                "\nAssertion failed for flow! Actual value: ${firstValue.destination}\n"
            )

            destination.onResult?.invoke(outputToReturn)
        }
    }

    fun assertModalViewController(
        eventsDispatcher: EventsDispatcher,
        assertion: (ViewController<*>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val controller = (firstValue.destination as ModalDestination.CallbackController).controller
            Assertions.assertTrue(
                assertion(controller as ViewController<*>),
                "\nAssertion failed for viewController! Actual value: ${firstValue.destination}\n"
            )
        }
    }

    fun assertDestination(
        destination: NavigationDestination,
        eventsDispatcher: EventsDispatcher,
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            Assertions.assertEquals(
                destination,
                firstValue.destination,
                "\nAssertion failed for destination!"
            )
        }
    }

    fun assertNoNavigationEvent(
        eventsDispatcher: EventsDispatcher,
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher, never()).handleEvent(capture())
            clearInvocations(eventsDispatcher)
        }
    }
}