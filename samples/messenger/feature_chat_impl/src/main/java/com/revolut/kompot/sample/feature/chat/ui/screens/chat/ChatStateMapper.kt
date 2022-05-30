package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import com.revolut.kompot.sample.feature.chat.data.USER_ID
import com.revolut.kompot.sample.feature.chat.domain.Message
import com.revolut.kompot.sample.feature.chat.ui.delegates.MessageRowDelegate
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract.UIState
import javax.inject.Inject

class ChatStateMapper @Inject constructor(
    private val datePrinter: DatePrinter
) : StateMapper<DomainState, UIState> {

    override fun mapState(domainState: DomainState): UIState {
        val inputText = domainState.messageInputText
        return UIState(
            contactName = domainState.contact.firstName,
            contactAvatar = domainState.contact.avatar,
            contactStatus = "Online",
            messageInputText = inputText,
            actionButtonEnabled = inputText.isNotEmpty(),
            items = createMessages(domainState.messages)
        )
    }

    private fun createMessages(messages: List<Message>) = messages.mapIndexed { i, message ->
        MessageRowDelegate.Model(
            listId = i.toString(),
            text = message.text,
            caption = datePrinter.printTime(message.timestamp),
            gravity = if (message.receiverId == USER_ID) {
                MessageRowDelegate.Model.Gravity.START
            } else {
                MessageRowDelegate.Model.Gravity.END
            },
            background = if (message.receiverId == USER_ID) {
                MessageRowDelegate.Model.Background.DARK
            } else {
                MessageRowDelegate.Model.Background.DEFAULT
            }
        )
    }

}