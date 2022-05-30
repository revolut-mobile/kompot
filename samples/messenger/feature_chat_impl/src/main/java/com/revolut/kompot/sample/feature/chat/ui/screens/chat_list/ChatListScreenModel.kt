package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenContract.*
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import javax.inject.Inject

class ChatListScreenModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageGenerator: MessageGenerator,
    stateMapper: StateMapper<DomainState, UIState>
) : BaseScreenModel<DomainState, UIState, OutputData>(stateMapper), ScreenModelApi {

    override val initialState = DomainState(emptyList())

    override fun onCreated() {
        super.onCreated()

        messageGenerator.generateMessage()

        chatRepository.chatListStream()
            .collectTillFinish(
                onEach = { chatList ->
                    updateState {
                        copy(chats = chatList)
                    }
                }
            )
    }

    override fun onRowClicked(contact: Contact) {
        ChatNavigationDestination(
            inputData = ChatNavigationDestination.InputData(contact)
        ).navigate()
    }

}