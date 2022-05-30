package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.revolut.kompot.coroutines.test.dispatchBlockingTest
import com.revolut.kompot.coroutines.test.flow.testIn
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.createSampleMessage
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.utils.date.provider.MockDateProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test

class ChatScreenModelTest {

    private val contact = Contact(
        id = 1,
        firstName = "John",
        lastName = "Newman",
        avatar = R.drawable.avatar_mc_fly
    )

    private val initialDomainState = ChatScreenContract.DomainState(
        contact = contact,
        messageInputText = "",
        messages = listOf()
    )

    private val mockDateProvider = MockDateProvider()
    private val messageGenerator: MessageGenerator = mock()

    private val chatMessages = listOf(createSampleMessage())
    private val chatRepository: ChatRepository = mock {
        on { messagesStream(contact.id) } doReturn flowOf(chatMessages)
        onBlocking { saveMessage(any(), any()) } doReturn contact.id
        onBlocking { markMessagesAsRead(contact.id) } doReturn Unit
    }

    private var screenModel = createScreenModel()

    @Test
    fun `should observe chat messages after created`() = dispatchBlockingTest {
        val testMessages = listOf(createSampleMessage())

        whenever(chatRepository.messagesStream(contact.id))
            .thenReturn(flowOf(testMessages))

        val streamTest = screenModel.domainStateStream().testIn(this)

        screenModel.onCreated()

        val expectedStates = listOf(
            initialDomainState,
            initialDomainState.copy(
                messages = testMessages
            )
        )

        streamTest.assertValues(expectedStates)

        verify(chatRepository).messagesStream(contact.id)
    }

    @Test
    fun `should read upcoming messages`() = dispatchBlockingTest {

        whenever(chatRepository.messagesStream(contact.id))
            .thenReturn(
                flowOf(
                    listOf(createSampleMessage()),
                    listOf(createSampleMessage(), createSampleMessage())
                )
            )

        screenModel.onCreated()


        verify(chatRepository, atLeast(2)).markMessagesAsRead(contact.id)
        verify(chatRepository).messagesStream(contact.id)
    }

    @Test
    fun `should update state with new input`() = dispatchBlockingTest {
        val input = "hello"

        val streamTest = screenModel.domainStateStream().testIn(this)

        val expectedStates = listOf(
            initialDomainState,
            initialDomainState.copy(
                messageInputText = input
            )
        )


        screenModel.onInputChanged(input)

        streamTest.assertValues(expectedStates)
    }

    @Test
    fun `should clear input when action button clicked`() = dispatchBlockingTest {
        val initialInput = "hello"

        val streamTest = screenModel.domainStateStream().testIn(this)

        screenModel.onCreated()
        screenModel.onInputChanged(initialInput)
        screenModel.onActionButtonClick()

        val expectedStates = listOf(
            initialDomainState,
            initialDomainState.copy(
                messages = chatMessages,
                messageInputText = ""
            ),
            initialDomainState.copy(
                messages = chatMessages,
                messageInputText = initialInput
            ),
            initialDomainState.copy(
                messages = chatMessages,
                messageInputText = ""
            )
        )

        streamTest.assertValues(expectedStates)
    }

    @Test
    fun `should send message after button clicked`() = dispatchBlockingTest {
        val messageText = "hello"

        screenModel.onCreated()

        screenModel.onInputChanged(messageText)
        screenModel.onActionButtonClick()

        val expectedMessage = Message.createFromUser(
            contactId = contact.id,
            text = messageText,
            date = mockDateProvider.provideDate()
        )

        verify(chatRepository).saveMessage(contact, expectedMessage)
    }

    @Test
    fun `should launch response generator after button clicked`() {
        screenModel.onCreated()
        screenModel.onActionButtonClick()
        verify(messageGenerator).generateMessage(contact.id)
    }

    private fun createScreenModel(
        contact: Contact = this.contact
    ) = ChatScreenModel(
        inputData = ChatNavigationDestination.InputData(
            contact = contact
        ),
        chatRepository = chatRepository,
        dateProvider = mockDateProvider,
        messageGenerator = messageGenerator,
        stateMapper = mock()
    )

}