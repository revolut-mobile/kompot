package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.core.test.assertion.resultStream
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.navigable.vc.test.testDomainStateStream
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.InputType
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.OutputData
import org.junit.jupiter.api.Test

class InputViewModelTest {

    @Test
    fun `should have input type in domain state`() = dispatchBlockingTest {
        val model = createModel(InputType.FIRST_NAME)

        val streamTest = model.testDomainStateStream().testIn(this)

        model.onCreated()

        val expectedState = DomainState(InputType.FIRST_NAME, "")

        streamTest.assertValues(expectedState)
    }

    @Test
    fun `should update state with new input`() = dispatchBlockingTest {
        val model = createModel(InputType.FIRST_NAME)

        val streamTest = model.testDomainStateStream().testIn(this)

        val input = "hello"

        model.onCreated()
        model.onInputChanged(input)

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
        val model = createModel()

        val streamTest = model.resultStream().testIn(this)

        val input = "hello"

        model.onCreated()
        model.onInputChanged(input)
        model.onActionClick()

        streamTest.assertValues(OutputData(input))
    }

    private fun createModel(inputType: InputType = InputType.FIRST_NAME) = InputViewModel(
        stateMapper = mock(),
        inputType = inputType,
    )
}