package com.revolut.kompot.sample.feature.chat

import com.revolut.kompot.FeatureGateway
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.sample.feature.chat.di.ChatArguments
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatViewController

class ChatFeatureGateway(argsProvider: () -> ChatArguments) : FeatureGateway {

    init {
        ChatsApiProvider.init(argsProvider)
    }

    override fun getController(
        destination: NavigationDestination,
        flowModel: BaseFlowModel<*, *, *>
    ): Controller? = when (destination) {
        is ChatNavigationDestination -> ChatViewController(destination.inputData)
        else -> null
    }

    override fun clearReference() {
        ChatsApiProvider.clear()
    }

}