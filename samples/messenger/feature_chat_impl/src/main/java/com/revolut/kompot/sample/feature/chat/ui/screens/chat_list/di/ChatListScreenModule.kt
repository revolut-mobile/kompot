package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenContract.*
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenModel
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListStateMapper
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class ChatListScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun bindMapper(mapper: ChatListStateMapper): StateMapper<DomainState, UIState>

    @Binds
    @ScreenScope
    abstract fun bindScreenModel(model: ChatListScreenModel): ScreenModelApi

    @Module
    companion object {
        @Provides
        @JvmStatic
        @ScreenScope
        fun provideMessageGenerator(screen: BaseScreen<*, *, *>): MessageGenerator =
            MessageGeneratorImpl(screen.activity)
    }

}