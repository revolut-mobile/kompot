/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.sample.feature.chat

import com.revolut.kompot.DestinationHandlingResult
import com.revolut.kompot.FeatureFlowStep
import com.revolut.kompot.FeatureHandlerDelegate
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.feature.chat.api.ChatApi
import com.revolut.kompot.sample.feature.chat.di.ChatArguments
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreen
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreen
import kotlinx.parcelize.Parcelize

class ChatFeatureHandlerDelegate(
    argsProvider: () -> ChatArguments
) : FeatureHandlerDelegate<ChatArguments, ChatApi, ChatFeatureFlowStep>(argsProvider) {

    init {
        ChatsApiProvider.init(argsProvider)
    }

    override fun canHandleFeatureFlowStep(featureStep: FeatureFlowStep): Boolean = featureStep is ChatFeatureFlowStep

    override fun getController(step: ChatFeatureFlowStep, flowModel: BaseFlowModel<*, *, *>): Controller = when (step) {
        ChatFeatureFlowStep.ChatList -> ChatListScreen()
        is ChatFeatureFlowStep.Chat -> ChatScreen(step.inputData)
    }

    override fun handleDestination(destination: NavigationDestination): DestinationHandlingResult? = when (destination) {
        is ChatNavigationDestination -> DestinationHandlingResult(ChatFeatureFlowStep.Chat(destination.inputData))
        else -> null
    }

    override fun clearReference() {
        ChatsApiProvider.clear()
    }

}

sealed class ChatFeatureFlowStep : FeatureFlowStep {
    @Parcelize
    object ChatList : ChatFeatureFlowStep()

    @Parcelize
    data class Chat(val inputData: ChatNavigationDestination.InputData) : ChatFeatureFlowStep()
}