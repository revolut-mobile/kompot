package com.revolut.kompot.sample.feature.chat.ui.screens.chat.extension

import com.revolut.kompot.navigable.extension.StatefulControllerModelExtension

interface ChatMessageActionExtension : StatefulControllerModelExtension<ChatMessageActionExtension.DomainState> {

    fun onMessageClicked(listId: String)

    data class DomainState(val easterEggDiscovered: Boolean)

}