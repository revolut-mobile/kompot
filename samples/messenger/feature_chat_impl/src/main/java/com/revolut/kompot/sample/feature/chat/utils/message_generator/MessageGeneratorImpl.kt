package com.revolut.kompot.sample.feature.chat.utils.message_generator

import android.content.Context
import com.revolut.kompot.sample.feature.chat.data.messenger.MessengerService

class MessageGeneratorImpl(
    private val context: Context
) : MessageGenerator {

    override fun generateMessage(senderId: Long?) {
        context.startService(MessengerService.getGenerateMessagesIntent(context, senderId))
    }

}