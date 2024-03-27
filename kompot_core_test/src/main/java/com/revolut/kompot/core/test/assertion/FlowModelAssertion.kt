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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.ErrorEvent
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.coroutines.test.TestContextProvider
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.Back
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.Flow
import com.revolut.kompot.navigable.flow.FlowNavigationCommand
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.flow.PostFlowResult
import com.revolut.kompot.navigable.flow.Quit
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlow
import com.revolut.kompot.navigable.screen.Screen
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.flow.FlowViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.jupiter.api.Assertions
import java.util.LinkedList
import java.util.Queue

fun <STEP : FlowStep, OUTPUT : IOData.Output> BaseFlowModel<*, STEP, OUTPUT>.test()
        : FlowModelAssertion<STEP, OUTPUT> = BaseFlowModelAssertion(this)

fun <T, STEP : FlowStep, OUTPUT : IOData.Output> T.test()
        : FlowModelAssertion<STEP, OUTPUT> where T : FlowViewModel<STEP, OUTPUT>,
                                                 T : ViewControllerModel<OUTPUT> =
    ViewControllerFlowModelAssertion(this)

interface FlowModelAssertion<STEP : FlowStep, OUTPUT : IOData.Output> {

    fun <T : IOData.Output> assertStep(step: STEP, result: T): FlowModelAssertion<STEP, OUTPUT>

    fun <T : IOData.Output> assertStep(
        step: STEP,
        result: T,
        controllerAccessor: (Controller) -> Unit,
    ): FlowModelAssertion<STEP, OUTPUT>

    fun assertStep(step: STEP): FlowModelAssertion<STEP, OUTPUT>
    fun assertResult(output: OUTPUT)
    fun assertBack(): FlowModelAssertion<STEP, OUTPUT>
    fun assertQuitFlow()
    fun assertDestination(destination: NavigationDestination)
    fun assertDestination(assertion: (NavigationDestination) -> Boolean)
    fun assertError(assertion: (Throwable) -> Boolean)
    fun assertModalScreen(assertion: (Screen<*>) -> Boolean): FlowModelAssertion<STEP, OUTPUT>
    fun assertHasBackStack(): FlowModelAssertion<STEP, OUTPUT>
    fun assertNoBackStack(): FlowModelAssertion<STEP, OUTPUT>

    fun <T : IOData.Output> assertModalViewController(
        assertion: (ViewController<in T>) -> Boolean,
        output: T
    ): FlowModelAssertion<STEP, OUTPUT>

    fun assertModalScreenFromFlowCoordinator(
        assertion: (Screen<*>) -> Boolean,
    ): FlowModelAssertion<STEP, OUTPUT>

    fun <T : IOData.Output> assertModalScreen(
        output: T,
        assertion: (Screen<T>) -> Boolean
    ): FlowModelAssertion<STEP, OUTPUT>

    fun assertModalFlow(assertion: (Flow<*>) -> Boolean): FlowModelAssertion<STEP, OUTPUT>

    fun <T : IOData.Output> assertModalFlow(
        output: T,
        assertion: (Flow<T>) -> Boolean
    ): FlowModelAssertion<STEP, OUTPUT>

    fun assertDialog(
        model: DialogModel<*>,
        result: DialogModelResult
    ): FlowModelAssertion<STEP, OUTPUT>

    fun <MODEL : DialogModel<RESULT>, RESULT : DialogModelResult> assertDialog(
        assertion: (actual: MODEL) -> RESULT?
    ): FlowModelAssertion<STEP, OUTPUT>
}

internal abstract class CommonFlowModelAssertions<STEP : FlowStep, OUTPUT : IOData.Output> :
    FlowModelAssertion<STEP, OUTPUT> {
    protected val testScope = TestContextProvider.unconfinedTestScope()

    private val dialogResultStream = MutableSharedFlow<DialogModelResult>(extraBufferCapacity = 16)
    protected val dialogDisplayer = DialogDisplayer(
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
    protected val commandQueue: Queue<FlowNavigationCommand<STEP, OUTPUT>> = LinkedList()

    abstract val eventsDispatcher: EventsDispatcher
    abstract val hasBackStack: Boolean
    abstract fun getCurrentController(): Controller
    abstract fun getCurrentStep(): STEP

    override fun <T : IOData.Output> assertStep(step: STEP, result: T) = apply {
        assertStepInternal(step)
        finishStepWithResult(getCurrentController(), result)
    }

    override fun <T : IOData.Output> assertStep(
        step: STEP,
        result: T,
        controllerAccessor: (Controller) -> Unit,
    ): FlowModelAssertion<STEP, OUTPUT> {
        assertStepInternal(step)

        val controller = getCurrentController()
        controllerAccessor.invoke(controller)

        finishStepWithResult(controller, result)
        return this
    }

    override fun assertStep(step: STEP) = apply {
        assertStepInternal(step)
    }

    private fun assertStepInternal(step: STEP) {
        Assertions.assertEquals(
            step,
            getCurrentStep(),
            "\nCurrent step is different than expected!"
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : IOData.Output> finishStepWithResult(controller: Controller, result: T) {
        when (controller) {
            is Flow<*> -> (controller as Flow<T>).onFlowResult(result)
            is ScrollerFlow<*> -> (controller as ScrollerFlow<T>).onFlowResult(result)
            is Screen<*> -> (controller as Screen<T>).onScreenResult(result)
            is ViewController<*> -> (controller as ViewController<T>).postResult(result)
        }
    }

    override fun assertResult(output: OUTPUT) {
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

    override fun assertBack() = apply {
        val lastCommand = commandQueue.poll()
        Assertions.assertTrue(
            lastCommand is Back,
            "\nExpected Back but was: $lastCommand!"
        )
    }

    override fun assertQuitFlow() {
        val lastCommand = commandQueue.poll()
        Assertions.assertTrue(
            lastCommand is Quit,
            "\nExpected Quit but was: $lastCommand!"
        )
    }

    override fun assertDestination(destination: NavigationDestination) {
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

    override fun assertDestination(assertion: (NavigationDestination) -> Boolean) {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            Assertions.assertTrue(
                assertion(firstValue.destination),
                "\nActual: ${firstValue.destination}\n"
            )
        }
    }

    override fun assertError(assertion: (Throwable) -> Boolean) {
        argumentCaptor<ErrorEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            Assertions.assertTrue(
                assertion(firstValue.throwable),
                "\nActual: ${firstValue.throwable}\n"
            )
        }
    }

    override fun assertModalScreen(assertion: (Screen<*>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val screen = (firstValue.destination as ModalDestination.ExplicitScreen<*>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IOData.Output> assertModalScreen(
        output: T,
        assertion: (Screen<T>) -> Boolean
    ) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val destination = firstValue.destination
            val screen = (destination as ModalDestination.ExplicitScreen<T>).screen
            Assertions.assertTrue(
                assertion(screen),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )

            destination.onResult?.invoke(output)
        }
    }

    override fun <T : IOData.Output> assertModalViewController(
        assertion: (ViewController<in T>) -> Boolean,
        output: T
    ): FlowModelAssertion<STEP, OUTPUT> {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val destination = firstValue.destination
            val viewController = (destination as ModalDestination.CallbackController).controller
            Assertions.assertTrue(viewController is ViewController<*>, "$viewController is not instance of ViewController")
            require(viewController is ViewController<*>)
            Assertions.assertTrue(
                assertion(viewController as ViewController<T>),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )

            viewController.postResult(output)
        }
        return this
    }

    override fun assertModalScreenFromFlowCoordinator(assertion: (Screen<*>) -> Boolean): FlowModelAssertion<STEP, OUTPUT> {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val destination = firstValue.destination
            val viewController = (destination as ModalDestination.CallbackController).controller
            Assertions.assertTrue(viewController is Screen<*>, "$viewController is not instance of Screen")
            require(viewController is Screen<*>)
            Assertions.assertTrue(
                assertion(viewController as Screen<*>),
                "\nAssertion failed for screen: ${firstValue.destination}!"
            )
        }
        return this
    }

    override fun assertModalFlow(assertion: (Flow<*>) -> Boolean) = apply {
        argumentCaptor<NavigationEvent>().apply {
            verify(eventsDispatcher).handleEvent(capture())
            clearInvocations(eventsDispatcher)
            val flow = (firstValue.destination as ModalDestination.ExplicitFlow<*>).flow
            Assertions.assertTrue(
                assertion(flow),
                "\nAssertion failed for flow: ${firstValue.destination}!"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IOData.Output> assertModalFlow(output: T, assertion: (Flow<T>) -> Boolean) =
        apply {
            argumentCaptor<NavigationEvent>().apply {
                verify(eventsDispatcher).handleEvent(capture())
                clearInvocations(eventsDispatcher)
                val destination = firstValue.destination
                val flow = (destination as ModalDestination.ExplicitFlow<T>).flow
                Assertions.assertTrue(
                    assertion(flow),
                    "\nAssertion failed for screen: ${firstValue.destination}!"
                )

                destination.onResult?.invoke(output)
            }
        }

    override fun assertDialog(model: DialogModel<*>, result: DialogModelResult) = apply {
        Assertions.assertEquals(
            model,
            dialogQueue.poll(),
            "\nDialog model is different than expected!"
        )
        dialogResultStream.tryEmit(result)
    }

    override fun <MODEL : DialogModel<RESULT>, RESULT : DialogModelResult> assertDialog(assertion: (actual: MODEL) -> RESULT?) = apply {
        @Suppress("UNCHECKED_CAST") val actualModel = dialogQueue.poll() as MODEL
        val result = assertion(actualModel)
        result?.let { dialogResultStream.tryEmit(result) }
    }

    override fun assertHasBackStack(): FlowModelAssertion<STEP, OUTPUT> = apply {
        Assertions.assertTrue(hasBackStack, "Expected hasBackStack true, but got false")
    }

    override fun assertNoBackStack(): FlowModelAssertion<STEP, OUTPUT> = apply {
        Assertions.assertFalse(hasBackStack, "Expected hasBackStack false, but got true")
    }
}