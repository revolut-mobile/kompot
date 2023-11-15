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
import com.revolut.kompot.coroutines.test.KompotTestScope
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.EmptyDialogModelResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.exceptions.verification.TooFewActualInvocations
import org.mockito.exceptions.verification.TooManyActualInvocations
import org.mockito.exceptions.verification.WantedButNotInvoked
import org.opentest4j.AssertionFailedError


internal class ScreenModelAssertionTest {

    private val screenModel = FakeScreenModel()

    @Test
    fun `GIVEN no navigation to destination WHEN destination asserted THEN catch assert exception`() {
        assertThrows<WantedButNotInvoked> {
            screenModel.assertDestination(DummyNavigationDestination())
        }
    }

    @Test
    fun `GIVEN navigation to destination WHEN destination asserted THEN pass the test`() {
        testAssertDestination()
    }

    @Test
    fun `GIVEN navigation to destination twice WHEN destination asserted twice THEN pass the test`() {
        repeat(2) {
            testAssertDestination()
        }
    }

    private fun testAssertDestination() {
        val destination = DummyNavigationDestination()

        screenModel.navigateToDestination(destination)

        screenModel.assertDestination(destination)
    }

    @Test
    fun `GIVEN navigation to destination WHEN destination asserted twice THEN catch proper exception`() {
        val destination = DummyNavigationDestination()

        screenModel.navigateToDestination(destination)

        screenModel.assertDestination(destination)
        assertThrows<WantedButNotInvoked> {
            screenModel.assertDestination(destination)
        }
    }

    @Test
    fun `GIVEN navigation to destination WHEN destination asserted with custom assertion THEN pass the test`() {
        testAssertDestinationWithCustomAssertion()
    }

    @Test
    fun `GIVEN navigation to destination twice WHEN destination asserted with custom assertion twice THEN pass the test`() {
        repeat(2) {
            testAssertDestinationWithCustomAssertion()
        }
    }

    private fun testAssertDestinationWithCustomAssertion() {
        val destination = DummyNavigationDestination()

        screenModel.navigateToDestination(destination)

        screenModel.assertDestination { actualDestination -> actualDestination == destination }
    }

    @Test
    fun `GIVEN navigation to destination WHEN destination asserted with custom assertion twice THEN catch proper exception`() {
        val destination = DummyNavigationDestination()

        screenModel.navigateToDestination(destination)

        screenModel.assertDestination { actualDestination -> actualDestination == destination }
        assertThrows<WantedButNotInvoked> {
            screenModel.assertDestination { actualDestination -> actualDestination == destination }
        }
    }

    @Test
    fun `GIVEN no navigation to destination WHEN destination asserted with custom assertion THEN catch proper exception`() {
        val destination = DummyNavigationDestination()

        assertThrows<WantedButNotInvoked> {
            screenModel.assertDestination { actualDestination -> actualDestination == destination }
        }
    }

    @Test
    fun `GIVEN error event WHEN error asserted with custom assertion THEN pass the test`() {
        testAssertError()
    }

    @Test
    fun `GIVEN error event twice WHEN error asserted with custom assertion twice THEN pass the test`() {
        repeat(2) {
            testAssertError()
        }
    }

    private fun testAssertError() {
        val error = Throwable()

        screenModel.postError(error)

        screenModel.assertError { actualError -> actualError == error }
    }

    @Test
    fun `GIVEN error event WHEN error asserted with custom assertion twice THEN catch proper exception`() {
        val error = Throwable()

        screenModel.postError(error)

        screenModel.assertError { actualError -> actualError == error }
        assertThrows<WantedButNotInvoked> {
            screenModel.assertError { actualError -> actualError == error }
        }
    }

    @Test
    fun `GIVEN no error event WHEN error asserted with custom assertion THEN catch proper exception`() {
        val error = Throwable()

        assertThrows<WantedButNotInvoked> {
            screenModel.assertError { actualError -> actualError == error }
        }
    }

    @Test
    fun `GIVEN modal screen started WHEN modal screen asserted THEN pass the test`() {
        testAssertModalScreen()
    }

    @Test
    fun `GIVEN modal screen started twice WHEN modal screen asserted twice THEN pass the test`() {
        repeat(2) {
            testAssertModalScreen()
        }
    }

    @Test
    fun `GIVEN modal screen started WHEN modal screen asserted twice THEN catch proper exception`() {
        testAssertModalScreen()
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalScreen { screen -> screen is DummyScreen<*, *, *> }
        }
    }

    private fun testAssertModalScreen() {
        screenModel.startModalScreen()

        screenModel.assertModalScreen { screen -> screen is DummyScreen<*, *, *> }
        Assertions.assertFalse(screenModel.modalScreenResultHandled)
    }

    @Test
    fun `GIVEN no modal screen started WHEN modal screen asserted THEN catch proper exception`() {
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalScreen { screen -> screen is DummyScreen<*, *, *> }
        }
        Assertions.assertFalse(screenModel.modalScreenResultHandled)
    }

    @Test
    fun `GIVEN modal screen with result started WHEN modal screen asserted THEN pass the test`() {
        testAssertModalScreenWithResult()
    }

    @Test
    fun `GIVEN modal screen with result started WHEN modal screen asserted twice THEN throw proper exception`() {
        testAssertModalScreenWithResult()
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalScreen(IOData.EmptyOutput) { screen -> screen is DummyScreen<*, *, *> }
        }
    }

    @Test
    fun `GIVEN modal screen with result started twice WHEN modal screen asserted twice THEN pass the test`() {
        repeat(2) {
            testAssertModalScreenWithResult()
        }
    }

    private fun testAssertModalScreenWithResult() {
        screenModel.startModalScreen()

        screenModel.assertModalScreen(IOData.EmptyOutput) { screen -> screen is DummyScreen<*, *, *> }
        Assertions.assertTrue(screenModel.modalScreenResultHandled)
    }

    @Test
    fun `GIVEN modal flow started WHEN modal flow asserted THEN pass the test`() {
        testAssertModalFlow()
    }

    @Test
    fun `GIVEN modal flow started twice WHEN modal flow asserted twice THEN pass the test`() {
        repeat(2) {
            testAssertModalFlow()
        }
    }

    @Test
    fun `GIVEN modal flow started WHEN modal flow asserted twice THEN catch proper exception`() {
        testAssertModalFlow()
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalFlow { flow -> flow is DummyFlow<*, *, *> }
        }
    }

    private fun testAssertModalFlow() {
        screenModel.startModalFlow()

        screenModel.assertModalFlow { flow -> flow is DummyFlow<*, *, *> }
        Assertions.assertFalse(screenModel.modalFlowResultHandled)
    }

    @Test
    fun `GIVEN no modal flow started WHEN modal flow asserted THEN catch proper exception`() {
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalFlow { flow -> flow is DummyFlow<*, *, *> }
        }
        Assertions.assertFalse(screenModel.modalFlowResultHandled)
    }

    @Test
    fun `GIVEN modal flow with result started WHEN modal flow asserted THEN pass the test`() {
        testAssertModalFlowWithResult()
    }

    @Test
    fun `GIVEN 2 modal flows with result started WHEN modal flows asserted THEN pass the test`() {
        repeat(2) {
            testAssertModalFlowWithResult()
        }
    }

    private fun testAssertModalFlowWithResult() {
        screenModel.startModalFlow()

        screenModel.assertModalFlow(IOData.EmptyOutput) { flow -> flow is DummyFlow<*, *, *> }
        Assertions.assertTrue(screenModel.modalFlowResultHandled)
    }

    @Test
    fun `GIVEN 1 modal flow with result started WHEN modal flow asserted twice THEN throw proper exception`() {
        testAssertModalFlowWithResult()
        assertThrows<WantedButNotInvoked> {
            screenModel.assertModalFlow(IOData.EmptyOutput) { flow -> flow is DummyFlow<*, *, *> }
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN dialog asserted THEN pass the test`() = dispatchBlockingTest {
        testDialogAssertion()
    }

    @Test
    fun `GIVEN 2 dialogs shown WHEN dialogs asserted THEN pass the test`() = dispatchBlockingTest {
        repeat(2) {
            testDialogAssertion()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN dialog asserted twice THEN throw proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()

        screenModel.mockDialogResult(dialogModel, EmptyDialogModelResult)

        screenModel.startDialog(dialogModel)
            .testIn(this)

        screenModel.assertDialog(dialogModel)
        assertThrows<WantedButNotInvoked> {
            screenModel.assertDialog(dialogModel)
        }
    }

    private fun KompotTestScope.testDialogAssertion() {
        val dialogModel = DummyDialogModel()

        screenModel.mockDialogResult(dialogModel, EmptyDialogModelResult)

        screenModel.startDialog(dialogModel)
            .testIn(this)

        screenModel.assertDialog(dialogModel)
    }

    @Test
    fun `GIVEN dialog not shown WHEN dialog asserted THEN catch proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()

        assertThrows<WantedButNotInvoked> {
            screenModel.assertDialog(dialogModel)
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN dialog asserted with custom assertion THEN pass the test`() = dispatchBlockingTest {
        testDialogWithCustomAssertion()
    }

    @Test
    fun `GIVEN dialog shown twice WHEN dialog asserted with custom assertion twice THEN pass the test`() = dispatchBlockingTest {
        repeat(2) {
            testDialogWithCustomAssertion()
        }
    }

    private fun KompotTestScope.testDialogWithCustomAssertion() {
        val dialogModel = DummyDialogModel()
        screenModel.mockDialogResult(dialogModel, EmptyDialogModelResult)

        val testObserver = screenModel.startDialog(dialogModel)
            .testIn(this)

        screenModel.assertDialog<DummyDialogModel, EmptyDialogModelResult> { model ->
            dialogModel == model
        }
        testObserver.assertValues(listOf(EmptyDialogModelResult))
    }

    @Test
    fun `GIVEN dialog shown WHEN dialog asserted with custom assertion twice THEN throw proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()
        screenModel.mockDialogResult(dialogModel, EmptyDialogModelResult)

        val testObserver = screenModel.startDialog(dialogModel)
            .testIn(this)

        screenModel.assertDialog<DummyDialogModel, EmptyDialogModelResult> { model ->
            dialogModel == model
        }
        testObserver.assertValues(listOf(EmptyDialogModelResult))
        assertThrows<WantedButNotInvoked> {
            screenModel.assertDialog<DummyDialogModel, EmptyDialogModelResult> { model ->
                dialogModel == model
            }
        }
    }

    @Test
    fun `GIVEN multiple dialogs shown WHEN dialogs asserted THEN pass the test`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()
        val anotherDialogModel = AnotherDialogModel()
        val thirdDialogModel = object : DialogModel<EmptyDialogModelResult> {}

        screenModel.showDialog(dialogModel)
        screenModel.showDialog(anotherDialogModel)
        screenModel.showDialog(thirdDialogModel)

        screenModel.assertDialogs(dialogModel, anotherDialogModel, thirdDialogModel)
    }

    @Test
    fun `GIVEN two dialogs shown WHEN one dialog asserted THEN throw proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()
        val anotherDialogModel = AnotherDialogModel()

        screenModel.showDialog(dialogModel)
        screenModel.showDialog(anotherDialogModel)

        assertThrows<TooManyActualInvocations> {
            screenModel.assertDialogs(dialogModel)
        }
    }

    @Test
    fun `GIVEN one dialog shown WHEN two dialogs asserted THEN throw proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()
        val anotherDialogModel = AnotherDialogModel()

        screenModel.showDialog(dialogModel)

        assertThrows<TooFewActualInvocations> {
            screenModel.assertDialogs(dialogModel, anotherDialogModel)
        }
    }

    @Test
    fun `GIVEN two dialogs shown WHEN dialogs asserted in wrong order THEN throw proper exception`() = dispatchBlockingTest {
        val dialogModel = DummyDialogModel()
        val anotherDialogModel = AnotherDialogModel()

        screenModel.showDialog(dialogModel)
        screenModel.showDialog(anotherDialogModel)

        assertThrows<AssertionFailedError> {
            screenModel.assertDialogs(anotherDialogModel, dialogModel)
        }
    }

    @Test
    fun `GIVEN dialog model WHEN dialog asserted THEN pass the test`() {
        val model = DummyDialogModel()

        screenModel.hideDialog(model)
        screenModel.assertDialogHidden<DummyDialogModel> {
            model == it
        }
    }

    internal class AnotherDialogModel : DialogModel<EmptyDialogModelResult>
}
