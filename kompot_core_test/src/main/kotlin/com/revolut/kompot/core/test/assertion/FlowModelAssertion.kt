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

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.revolut.kompot.core.test.assertion

import android.annotation.SuppressLint
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.coroutines.test.TestContextProvider
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogDisplayerDelegate
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.Next
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions
import java.util.*

fun <STEP : FlowStep, OUTPUT : IOData.Output> BaseFlowModel<*, STEP, OUTPUT>.test() = FlowModelAssertion(this)

@SuppressLint("CheckResult", "VisibleForTests")
class FlowModelAssertion<STEP : FlowStep, OUTPUT : IOData.Output> internal constructor(
    private val flowModel: BaseFlowModel<*, STEP, OUTPUT>
) {

    private val testScope = TestContextProvider.unconfinedTestScope()

    private val dialogResultStream = MutableSharedFlow<DialogModelResult>(extraBufferCapacity = 16)
    private val dialogDisplayer = DialogDisplayer(
        loadingDialogDisplayer = mock(),
        delegates = listOf(
            FakeDialogDisplayerDelegate(
                dialogResultStream = dialogResultStream,
                onShown = { model ->
                    dialogQueue.add(model)
                }
            )
        )
    )

    private val dialogQueue: Queue<DialogModel<*>> = LinkedList()
    private val commandQueue: Queue<FlowNavigationCommand<STEP, OUTPUT>> = LinkedList()

    init {
        flowModel.applyTestDependencies(dialogDisplayer = dialogDisplayer)
        flowModel.setInitialState()
        val childFlowModel = FakeFlowModel()
        flowModel
            .navigationBinder().asFlow()
            .onEach { command ->
                when (command) {
                    is Next -> flowModel.setNextState(
                        command.step, command.animation,
                        command.addCurrentStepToBackStack,
                        childFlowModel
                    )
                    is Back -> {
                        flowModel.restorePreviousState()
                        commandQueue.add(command)
                    }
                    else -> commandQueue.add(command)
                }
            }.launchIn(testScope)

        flowModel.onCreated()
    }

    fun <T : IOData.Output> assertStep(step: STEP, result: T) = apply {
        assertStepInternal(step)
        finishStepWithResult(result)
    }

    fun assertStep(step: STEP) = apply {
        assertStepInternal(step)
    }

    private fun assertStepInternal(step: STEP) {
        Assertions.assertEquals(
            step,
            flowModel.step,
            "\nCurrent step is different than expected!"
        )
    }

    @OptIn(ExperimentalKompotApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun <T : IOData.Output> finishStepWithResult(result: T) {
        when (val controller = flowModel.getController(flowModel.step)) {
            is Flow<*> -> (controller as Flow<T>).onFlowResult(result)
            is ScrollerFlow<*> -> (controller as ScrollerFlow<T>).onFlowResult(result)
            is Screen<*> -> (controller as Screen<T>).onScreenResult(result)
        }
    }

    fun assertResult(output: OUTPUT) {
        val lastCommand = commandQueue.poll()
        assertResult(lastCommand)
        val result = (lastCommand as PostFlowResult).data
        Assertions.assertEquals(
            output,
            result,
            "\nResult is different than expected!"
        )
    }

    private fun assertResult(command: FlowNavigationCommand<STEP, OUTPUT>?) {
        Assertions.assertTrue(
            command is PostFlowResult,
            "\nExpected PostFlowResult but was: $command!"
        )
    }

    fun assertBack() = apply {
        val lastCommand = commandQueue.poll()
        Assertions.assertTrue(
            lastCommand is Back,
            "\nExpected Back but was: $lastCommand!"
        )
    }

    fun assertQuitFlow() {
        val lastCommand = commandQueue.poll()
        Assertions.assertTrue(
            lastCommand is Quit,
            "\nExpected Quit but was: $lastCommand!"
        )
    }

    fun assertDestination(destination: NavigationDestination) {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            Assertions.assertEquals(
                destination,
                firstValue.destination,
                "\nAssertion failed for destination!"
            )
        }
    }

    fun assertDestination(assertion: (NavigationDestination) -> Boolean) {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            Assertions.assertTrue(
                assertion(firstValue.destination),
                "\nActual: ${firstValue.destination}\n"
            )
        }
    }

    fun assertError(assertion: (Throwable) -> Boolean) {
        argumentCaptor<ErrorEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            Assertions.assertTrue(
                assertion(firstValue.throwable),
                "\nActual: ${firstValue.throwable}\n"
            )
        }
    }

    fun assertModalScreen(assertion: (Screen<*>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            val screen = (firstValue.destination as ModalDestination.ExplicitScreen<*>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IOData.Output> assertModalScreen(output: T, assertion: (Screen<T>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            val destination = firstValue.destination
            val screen = (destination as ModalDestination.ExplicitScreen<T>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )

            destination.onResult?.invoke(output)
        }
    }

    fun assertModalFlow(assertion: (Flow<*>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            val flow = (firstValue.destination as ModalDestination.ExplicitFlow<*>).flow
            Assertions.assertTrue(
                assertion(flow),
                "\nAssertion failed for flow: ${firstValue.destination}!"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IOData.Output> assertModalFlow(output: T, assertion: (Flow<T>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(flowModel.eventsDispatcher).handleEvent(capture())
            clearInvocations(flowModel.eventsDispatcher)
            val destination = firstValue.destination
            val flow = (destination as ModalDestination.ExplicitFlow<T>).flow
            Assertions.assertTrue(
                assertion(flow),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )

            destination.onResult?.invoke(output)
        }
    }

    fun assertDialog(model: DialogModel<*>, result: DialogModelResult) = apply {
        Assertions.assertEquals(
            model,
            dialogQueue.poll(),
            "\nDialog model is different than expected!"
        )
        dialogResultStream.tryEmit(result)
    }

    fun <MODEL : DialogModel<RESULT>, RESULT : DialogModelResult> assertDialog(assertion: (actual: MODEL) -> RESULT?) {
        @Suppress("UNCHECKED_CAST") val actualModel = dialogQueue.poll() as MODEL
        val result = assertion(actualModel)
        result?.let { dialogResultStream.tryEmit(result) }
    }
}

@SuppressLint("VisibleForTests")
private class FakeFlowModel : BaseFlowModel<FakeState, FakeStep, IOData.EmptyOutput>() {

    override val initialStep: FakeStep = FakeStep
    override val initialState: FakeState = FakeState

    init {
        setInitialState()
    }

    override fun getController(step: FakeStep): Controller {
        throw IllegalStateException()
    }
}

@Parcelize
private object FakeStep : FlowStep

@Parcelize
private object FakeState : FlowState

private class FakeDialogDisplayerDelegate(
    private val dialogResultStream: kotlinx.coroutines.flow.Flow<DialogModelResult>,
    private val onShown: (DialogModel<*>) -> Unit
) : DialogDisplayerDelegate<DialogModel<*>>() {

    override fun canHandle(dialogModel: DialogModel<*>) = true

    override fun showDialogInternal(dialogModel: DialogModel<*>) {
        onShown(dialogModel)
    }

    override fun hideDialog() {
        //do nothing
    }

    override fun startObservingResult(): kotlinx.coroutines.flow.Flow<DialogModelResult> = dialogResultStream
}