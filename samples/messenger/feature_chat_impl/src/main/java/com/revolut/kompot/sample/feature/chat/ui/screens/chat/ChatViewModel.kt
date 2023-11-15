package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.SaveStateDelegate
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.ModelApi
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.RetainedState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.extension.ChatMessageActionExtension
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.utils.date.provider.DateProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val inputData: ChatNavigationDestination.InputData,
    private val chatRepository: ChatRepository,
    private val dateProvider: DateProvider,
    private val messageGenerator: MessageGenerator,
    private val chatMessageActionExtension: ChatMessageActionExtension,
    stateMapper: States.Mapper<DomainState, UIState>,
) : ViewControllerModel<IOData.EmptyOutput>(), ModelApi {

    override val state = ModelState(
        initialState = DomainState(
            contact = inputData.contact,
            messages = emptyList(),
            messageInputText = ""
        ),
        stateMapper = stateMapper,
        saveStateDelegate = ChatSaveStateDelegate(),
    )

    override fun onCreated() {
        super.onCreated()

        chatRepository.messagesStream(inputData.contact.id)
            .distinctUntilChanged()
            .collectTillFinish { messages ->
                state.update {
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
        chatMessageActionExtension.stateStream()
            .collectTillHide { delegateState ->
                if (delegateState.easterEggDiscovered) {
                    state.update {
                        copy(messageInputText = "Hehehe, thank you!")
                    }
                }
            }
    }

    override fun onInputChanged(text: String) {
        state.update {
            copy(messageInputText = text)
        }
    }

    override fun onActionButtonClick() {
        val contactId = inputData.contact.id
        val message = Message.createFromUser(
            contactId = contactId,
            text = state.current.messageInputText,
            date = dateProvider.provideDate()
        )
        tillFinish {
            val receiverId = chatRepository.saveMessage(inputData.contact, message)
            messageGenerator.generateMessage(receiverId)
        }
        state.update {
            copy(messageInputText = "")
        }
    }

    override fun onMessageClicked(listId: String) {
        chatMessageActionExtension.onMessageClicked(listId)
    }

}

class ChatSaveStateDelegate : SaveStateDelegate<DomainState, RetainedState>() {

    override fun getRetainedState(currentState: DomainState) =
        RetainedState(currentState.messageInputText)

    override fun restoreDomainState(
        initialState: DomainState,
        retainedState: RetainedState
    ) = initialState.copy(
        messageInputText = retainedState.messageInputText
    )

}