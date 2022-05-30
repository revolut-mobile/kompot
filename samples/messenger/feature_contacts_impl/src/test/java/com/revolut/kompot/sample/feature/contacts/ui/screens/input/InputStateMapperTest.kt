package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InputStateMapperTest {

    private val stateMapper = InputStateMapper()

    @Test
    fun `should map state for name input`() {
        val domainState = DomainState(inputType = InputType.FIRST_NAME, inputText = "John")

        val expected = UIState(inputHint = "Input first name", inputText = "John")
        assertEquals(expected, stateMapper.mapState(domainState))
    }

    @Test
    fun `should map state for surname input`() {
        val domainState = DomainState(inputType = InputType.LAST_NAME, inputText = "Newman")

        val expected = UIState(inputHint = "Input last name", inputText = "Newman")
        assertEquals(expected, stateMapper.mapState(domainState))
    }

}