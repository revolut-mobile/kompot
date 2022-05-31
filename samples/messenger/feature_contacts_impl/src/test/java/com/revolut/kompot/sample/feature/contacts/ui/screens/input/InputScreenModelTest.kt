package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.core.test.assertion.resultStream
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.*
import org.junit.jupiter.api.Test

class InputScreenModelTest {

    @Test
    fun `should have input type in domain state`() = dispatchBlockingTest {
        val screenModel = createScreenModel(InputType.FIRST_NAME)

        val streamTest = screenModel.domainStateStream().testIn(this)

        screenModel.onCreated()

        val expectedState = DomainState(InputType.FIRST_NAME, "")

        streamTest.assertValues(expectedState)
    }

    @Test
    fun `should update state with new input`() = dispatchBlockingTest {
        val screenModel = createScreenModel(InputType.FIRST_NAME)

        val streamTest = screenModel.domainStateStream().testIn(this)

        val input = "hello"

        screenModel.onCreated()
        screenModel.onInputChanged(input)

        val expectedStates = listOf(
            DomainState(
                inputType = InputType.FIRST_NAME,
                inputText = ""
            ),
            DomainState(
                inputType = InputType.FIRST_NAME,
                inputText = input
            )
        )

        streamTest.assertValues(expectedStates)
    }

    @Test
    fun `should return result with the latest input when action clicked`() = dispatchBlockingTest {
        val screenModel = createScreenModel()

        val streamTest = screenModel.resultStream().testIn(this)

        val input = "hello"

        screenModel.onCreated()
        screenModel.onInputChanged(input)
        screenModel.onActionClick()

        streamTest.assertValues(OutputData(input))
    }

    private fun createScreenModel(inputType: InputType = InputType.FIRST_NAME) = InputScreenModel(
        stateMapper = mock(),
        inputData = InputData(inputType)
    )

}