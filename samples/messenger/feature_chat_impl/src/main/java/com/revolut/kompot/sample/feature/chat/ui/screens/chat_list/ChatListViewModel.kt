package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.ui.ModelState
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.ModelApi
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.OutputData
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.UIState
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import javax.inject.Inject

class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageGenerator: MessageGenerator,
    stateMapper: States.Mapper<DomainState, UIState>
) : ViewControllerModel<OutputData>(), ModelApi {

    override val state = ModelState(
        initialState = DomainState(emptyList()),
        stateMapper = stateMapper,
    )

    override fun onCreated() {
        super.onCreated()

        messageGenerator.generateMessage()

        chatRepository.chatListStream()
            .collectTillFinish(
                onEach = { chatList ->
                    state.update {
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