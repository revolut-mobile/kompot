package com.revolut.kompot.sample.feature.chat.navigation

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.InternalDestination
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatNavigationDestination(
    override val inputData: InputData
) : InternalDestination<ChatNavigationDestination.InputData>(inputData) {

    @Parcelize
    data class InputData(
        val contact: Contact
    ) : IOData.Input

}