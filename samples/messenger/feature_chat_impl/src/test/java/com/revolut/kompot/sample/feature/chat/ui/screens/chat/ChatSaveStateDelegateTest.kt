package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.sample.feature.chat.createSampleContact
import com.revolut.kompot.sample.feature.chat.createSampleMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChatSaveStateDelegateTest {

    private val savedStateDelegate = ChatSaveStateDelegate()

    @Test
    fun `get retained state from domain state`() {
        val domainState = ChatScreenContract.DomainState(
            contact = createSampleContact(),
            messages = listOf(createSampleMessage()),
            messageInputText = "message text"
        )

        val expected = ChatScreenContract.RetainedState("message text")

        assertEquals(expected, savedStateDelegate.getRetainedState(domainState))
    }

    @Test
    fun `get domain state from initial state and retained state`() {
        val initialState = ChatScreenContract.DomainState(
            contact = createSampleContact(),
            messages = listOf(createSampleMessage()),
            messageInputText = ""
        )

        val retainedState = ChatScreenContract.RetainedState("message text")

        val expected = initialState.copy(
            messageInputText = "message text"
        )

        assertEquals(expected, savedStateDelegate.restoreDomainState(initialState, retainedState))
    }

}