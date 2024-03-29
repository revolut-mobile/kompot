package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.navigable.vc.test.testDomainStateStream
import com.revolut.kompot.sample.feature.chat.createSampleChat
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.DomainState
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChatListViewModelTest {

    private val chatList = listOf(createSampleChat())

    private val chatRepository: ChatRepository = mock {
        onBlocking { chatListStream() } doReturn flowOf(chatList)
    }
    private val messageGenerator: MessageGenerator = mock()

    private val viewModel = createModel()

    @Test
    fun `should load chat list after created`() = dispatchBlockingTest {

        val streamTest = viewModel.testDomainStateStream().testIn(this)

        viewModel.onCreated()

        val expectedStates = listOf(
            DomainState(
                chats = emptyList()
            ),
            DomainState(
                chats = chatList
            )
        )

        streamTest.assertValues(expectedStates)

        verify(chatRepository).chatListStream()
    }

    @Test
    fun `should generate message after created`() {
        viewModel.onCreated()

        verify(messageGenerator).generateMessage()
    }

    @Test
    fun `return result with selected contact after contact selected`() {
        val contact = createSampleChat().contact

        viewModel.onRowClicked(contact)

        argumentCaptor<NavigationEvent> {
            verify(viewModel.eventsDispatcher).handleEvent(capture())

            val expected = ChatNavigationDestination(
                inputData = ChatNavigationDestination.InputData(contact)
            )

            assertEquals(expected, firstValue.destination)
        }
    }

    private fun createModel() = ChatListViewModel(
        chatRepository = chatRepository,
        messageGenerator = messageGenerator,
        stateMapper = mock()
    ).apply {
        injectDependencies(mock(), mock(), mock())
    }

}