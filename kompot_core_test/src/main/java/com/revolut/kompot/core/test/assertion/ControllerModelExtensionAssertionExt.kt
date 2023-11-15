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

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController

fun ControllerModelExtension.assertModalScreen(assertion: (Screen<*>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(parentEventsDispatcher, assertion)
}

fun <T : IOData.Output> ControllerModelExtension.assertModalScreen(outputToReturn: T, assertion: (Screen<T>) -> Boolean) {
    ControllerModelAssertions.assertModalScreen(parentEventsDispatcher, outputToReturn, assertion)
}

fun ControllerModelExtension.assertModalFlow(assertion: (Flow<*>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(parentEventsDispatcher, assertion)
}

fun <T : IOData.Output> ControllerModelExtension.assertModalFlow(outputToReturn: T, assertion: (Flow<in T>) -> Boolean) {
    ControllerModelAssertions.assertModalFlow(parentEventsDispatcher, outputToReturn, assertion)
}

fun ControllerModelExtension.assertModalViewController(assertion: (ViewController<*>) -> Boolean) {
    ControllerModelAssertions.assertModalViewController(parentEventsDispatcher, assertion)
}

fun ControllerModelExtension.assertDestination(destination: NavigationDestination) {
    ControllerModelAssertions.assertDestination(destination, parentEventsDispatcher)
}

fun ControllerModelExtension.assertNoNavigationEvent() {
    ControllerModelAssertions.assertNoNavigationEvent(parentEventsDispatcher)
}
