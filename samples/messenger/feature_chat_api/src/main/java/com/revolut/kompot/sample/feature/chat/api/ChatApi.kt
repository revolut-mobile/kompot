package com.revolut.kompot.sample.feature.chat.api

import com.revolut.kompot.FeatureApi
import com.revolut.kompot.sample.feature.chat.data.ChatRepository

interface ChatApi : FeatureApi {

    val chatRepository: ChatRepository

}