package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di

import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract
import dagger.Subcomponent

@ViewControllerScope
@Subcomponent(
    modules = [ChatListControllerModule::class]
)
interface ChatListControllerComponent : ViewControllerComponent {
    val model: ChatListContract.ModelApi

    @Subcomponent.Builder
    interface Builder : ViewControllerComponent.Builder<ChatListControllerComponent, Builder>
}