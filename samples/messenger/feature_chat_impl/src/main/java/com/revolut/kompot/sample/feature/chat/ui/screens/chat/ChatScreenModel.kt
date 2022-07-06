package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.navigable.screen.state.SaveStateDelegate
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract.*
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.utils.date.provider.DateProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ChatScreenModel @Inject constructor(
    private val inputData: ChatNavigationDestination.InputData,
    private val chatRepository: ChatRepository,
    private val dateProvider: DateProvider,
    private val messageGenerator: MessageGenerator,
    stateMapper: StateMapper<DomainState, UIState>
) : BaseScreenModel<DomainState, UIState, IOData.EmptyOutput>(stateMapper), ScreenModelApi {

    override val initialState = DomainState(
        contact = inputData.contact,
        messages = emptyList(),
        messageInputText = ""
    )

    override val saveStateDelegate = ChatSaveStateDelegate()

    override fun onCreated() {
        super.onCreated()

        chatRepository.messagesStream(inputData.contact.id)
            .distinctUntilChanged()
            .collectTillFinish { messages ->
                updateState {
                    copy(messages = messages)
                }
                chatRepository.markMessagesAsRead(inputData.contact.id)
            }
    }

    override fun onShown(idleTimeMs: Long) {
        super.onShown(idleTimeMs)

        tillHide {
            chatRepository.markMessagesAsRead(inputData.contact.id)
        }
    }

    override fun onInputChanged(text: String) {
        updateState {
            copy(messageInputText = text)
        }
    }

    override fun onActionButtonClick() {
        val contactId = inputData.contact.id
        val message = Message.createFromUser(
            contactId = contactId,
            text = state.messageInputText,
            date = dateProvider.provideDate()
        )
        tillFinish {
            val receiverId = chatRepository.saveMessage(inputData.contact, message)
            messageGenerator.generateMessage(receiverId)
        }
        updateState {
            copy(messageInputText = "")
        }
    }

}

class ChatSaveStateDelegate : SaveStateDelegate<DomainState, RetainedState>() {

    override fun getRetainedState(currentState: DomainState) = RetainedState(currentState.messageInputText)

    override fun restoreDomainState(
        initialState: DomainState,
        retainedState: RetainedState
    ) = initialState.copy(
        messageInputText = retainedState.messageInputText
    )

}