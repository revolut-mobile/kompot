package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenContract
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(
    modules = [ChatListScreenModule::class]
)
interface ChatListScreenComponent : BaseScreenComponent {
    val screenModel: ChatListScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<ChatListScreenComponent, Builder> {
        @BindsInstance
        fun inputData(ioData: IOData.EmptyInput): Builder
    }
}