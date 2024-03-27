package com.revolut.kompot.sample.feature.chat.ui.screens.chat.extension

import com.revolut.kompot.navigable.extension.BaseStatefulControllerModelExtension
import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.utils.date.provider.DateProvider
import javax.inject.Inject
import kotlin.random.Random

class ChatMessageActionExtensionImpl @Inject constructor(
    private val inputData: ChatNavigationDestination.InputData,
    private val chatRepository: ChatRepository,
    private val dateProvider: DateProvider,
) : BaseStatefulControllerModelExtension<ChatMessageActionExtension.DomainState>(),
    ChatMessageActionExtension {

    override val initialState = ChatMessageActionExtension.DomainState(easterEggDiscovered = false)

    override fun onMessageClicked(listId: String) {
        if (state.easterEggDiscovered) return

        val easterEggDiscovered = Random.nextBoolean()
        if (easterEggDiscovered) {
            val message = Message.createToUser(
                contactId = inputData.contact.id,
                text = "\uD83C\uDF8A You've discovered the Easter Egg \uD83C\uDF8A",
                date = dateProvider.provideDate()
            )
            tillFinish {
                chatRepository.saveMessage(inputData.contact, message)
            }

            updateState {
                copy(easterEggDiscovered = easterEggDiscovered)
            }
        }
    }

}