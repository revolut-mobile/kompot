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
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions

fun BaseScreenModel<*, *, *>.assertDestination(destination: NavigationDestination) {
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

fun BaseScreenModel<*, *, *>.assertDestination(assertion: (NavigationDestination) -> Boolean) {
    argumentCaptor<NavigationEvent>().apply {
        verify(eventsDispatcher).handleEvent(capture())
        clearInvocations(eventsDispatcher)
        Assertions.assertTrue(
            assertion(firstValue.destination),
            "\nAssertion failed for destination! Actual value: ${firstValue.destination}\n"
        )
    }
}

fun BaseScreenModel<*, *, *>.assertError(assertion: (Throwable) -> Boolean) {
    argumentCaptor<ErrorEvent>().apply {
        verify(eventsDispatcher).handleEvent(capture())
        clearInvocations(eventsDispatcher)
        Assertions.assertTrue(
            assertion(firstValue.throwable),
            "\nAssertion failed for error! Actual value: ${firstValue.throwable}\n"
        )
    }
}

fun BaseScreenModel<*, *, *>.assertModalScreen(assertion: (Screen<*>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(eventsDispatcher, assertion)
}

fun <T : IOData.Output> BaseScreenModel<*, *, *>.assertModalScreen(outputToReturn: T, assertion: (Screen<T>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(eventsDispatcher, outputToReturn, assertion)
}

fun BaseScreenModel<*, *, *>.assertModalFlow(assertion: (Flow<*>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(eventsDispatcher, assertion)
}

fun <T : IOData.Output> BaseScreenModel<*, *, *>.assertModalFlow(outputToReturn: T, assertion: (Flow<in T>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(eventsDispatcher, outputToReturn, assertion)
}

fun BaseScreenModel<*, *, *>.assertModalViewController(assertion: (ViewController<*>) -> Boolean) {
    ControllerModelAssertions.assertModalViewController(eventsDispatcher, assertion)
}

fun <T : IOData.Output> BaseScreenModel<*, *, *>.assertModalViewController(outputToReturn: T, assertion: (ViewController<in T>) -> Boolean) {
    ControllerModelAssertions.assertModalViewController(eventsDispatcher, outputToReturn, assertion)
}

fun ViewControllerModel<*>.assertModalViewController(assertion: (ViewController<*>) -> Boolean) {
    ControllerModelAssertions.assertModalViewController(eventsDispatcher, assertion)
}

fun ViewControllerModel<*>.assertModalController(assertion: (Controller) -> Boolean) {
    ControllerModelAssertions.assertModalController(eventsDispatcher, assertion)
}

fun <T : IOData.Output> ViewControllerModel<*>.assertModalViewController(outputToReturn: T, assertion: (ViewController<*>) -> Boolean) {
    ControllerModelAssertions.assertModalViewController(eventsDispatcher, outputToReturn, assertion)
}

fun ViewControllerModel<*>.assertModalScreen(assertion: (Screen<*>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(eventsDispatcher, assertion)
}

fun <T : IOData.Output> ViewControllerModel<*>.assertModalScreen(outputToReturn: T, assertion: (Screen<*>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(eventsDispatcher, outputToReturn, assertion)
}

fun ViewControllerModel<*>.assertModalFlow(assertion: (Flow<*>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(eventsDispatcher, assertion)
}

fun <T : IOData.Output> ViewControllerModel<*>.assertModalFlow(outputToReturn: T, assertion: (Flow<in T>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(eventsDispatcher, outputToReturn, assertion)
}

fun ViewControllerModel<*>.assertDestination(destination: NavigationDestination) {
    ControllerModelAssertions.assertDestination(destination, eventsDispatcher)
}

fun BaseScreenModel<*, *, *>.mockDialogResult(forModel: DialogModel<*>, resultToReturn: DialogModelResult) {
    whenever(dialogDisplayer.showDialog(forModel))
        .doReturn(flowOf(resultToReturn))
}

fun BaseScreenModel<*, *, *>.assertDialog(model: DialogModel<*>) {
    ControllerModelAssertions.assertDialog(dialogDisplayer, model)
}

inline fun <reified MODEL : DialogModel<RESULT>, RESULT : DialogModelResult> BaseScreenModel<*, *, *>.assertDialog(assertion: (actual: MODEL) -> Boolean) {
    argumentCaptor<MODEL>().apply {
        verify(dialogDisplayer).showDialog(capture())
        clearInvocations(dialogDisplayer)
        val dialogModel = firstValue
        Assertions.assertTrue(
            assertion(dialogModel),
            "\nAssertion failed for dialog! Actual value: $firstValue\n"
        )
    }
}

inline fun <reified MODEL : DialogModel<*>> BaseScreenModel<*, *, *>.assertDialogHidden(assertion: (actual: MODEL) -> Boolean) {
    argumentCaptor<MODEL>().apply {
        verify(dialogDisplayer).hideDialog(capture())
        clearInvocations(dialogDisplayer)
        val dialogModel = firstValue
        Assertions.assertTrue(
            assertion(dialogModel),
            "\nAssertion failed for dialog! Actual value: $firstValue\n"
        )
    }
}

/** Useful for cases where we show 2+ dialogs one after another. `BaseScreenModel.assertDialog()` implementation
 *  doesn't support sequential invocation, in contrast to `FlowModelAssertion.assertDialog()` */
fun BaseScreenModel<*, *, *>.assertDialogs(vararg models: DialogModel<*>) {
    ControllerModelAssertions.assertDialogs(dialogDisplayer, *models)
}
