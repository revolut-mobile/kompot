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
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.Controller
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
            if ((destination as? ModalDestination.ExplicitScreen<T>) != null) {
                Assertions.assertTrue(
                    assertion(destination.screen),
                    "\nAssertion failed for screen! Actual value: ${firstValue.destination}\n"
                )

                destination.onResult?.invoke(outputToReturn)
                return
            }

            // when launched using modalCoordinator in ViewControllerModel
            if ((destination as? ModalDestination.CallbackController)?.controller != null) {
                Assertions.assertTrue(
                    assertion(destination.controller as Screen<T>),
                    "\nAssertion failed for screen! Actual value: ${firstValue.destination}\n"
                )

                (destination.controller as Screen<T>).onScreenResult.invoke(outputToReturn)
                return
            }

            throw IllegalStateException("Can't assert modal screen\n$destination is not supported")
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

    @Suppress("UNCHECKED_CAST")
    fun <T : IOData.Output> assertModalViewController(
        eventsDispatcher: EventsDispatcher,
        outputToReturn: T,
        assertion: (ViewController<in T>) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val controller = (firstValue.destination as ModalDestination.CallbackController).controller
            Assertions.assertTrue(
                assertion(controller as ViewController<T>),
                "\nAssertion failed for viewController! Actual value: ${firstValue.destination}\n"
            )
            controller.postResult(outputToReturn)
        }
    }

    fun assertModalController(
        eventsDispatcher: EventsDispatcher,
        assertion: (Controller) -> Boolean
    ) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val controller = (firstValue.destination as ModalDestination.CallbackController).controller
            Assertions.assertTrue(
                assertion(controller),
                "\nAssertion failed for controller! Actual value: ${firstValue.destination}\n"
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

    fun assertDialog(dialogDisplayer: DialogDisplayer, model: DialogModel<*>) {
        argumentCaptor<DialogModel<DialogModelResult>>().apply {
            verify(dialogDisplayer).showDialog(capture())
            clearInvocations(dialogDisplayer)
            val dialogModel = firstValue
            Assertions.assertEquals(
                model,
                dialogModel,
                "\nAssertion failed for dialog!"
            )
        }
    }

    /** Useful for cases where we show 2+ dialogs one after another. `assertDialog()` implementation above
     *  doesn't support sequential invocation, in contrast to `FlowModelAssertion.assertDialog()` */
    fun assertDialogs(dialogDisplayer: DialogDisplayer, vararg models: DialogModel<*>) {
        argumentCaptor<DialogModel<DialogModelResult>>().apply {
            verify(dialogDisplayer, times(models.size)).showDialog(capture())
            clearInvocations(dialogDisplayer)
            models.forEachIndexed { index, model ->
                val dialogModel = allValues[index]
                Assertions.assertEquals(
                    model,
                    dialogModel,
                    "\nAssertion failed for dialog!"
                )
            }
        }
    }

    fun assertNoDialog(dialogDisplayer: DialogDisplayer) {
        argumentCaptor<DialogModel<DialogModelResult>>().apply {
            verify(dialogDisplayer, never()).showDialog(capture())
            clearInvocations(dialogDisplayer)
        }
    }
}
