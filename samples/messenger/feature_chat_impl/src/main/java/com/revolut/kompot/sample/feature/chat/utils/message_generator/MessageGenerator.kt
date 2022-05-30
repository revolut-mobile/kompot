package com.revolut.kompot.sample.feature.chat.utils.message_generator

interface MessageGenerator {

    fun generateMessage(senderId: Long? = null)

}