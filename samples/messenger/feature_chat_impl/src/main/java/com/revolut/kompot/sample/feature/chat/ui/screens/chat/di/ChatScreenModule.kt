package com.revolut.kompot.sample.feature.chat.ui.screens.chat.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenModel
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract.*
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatStateMapper
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class ChatScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun bindsChatScreenModel(chatScreenModel: ChatScreenModel): ScreenModelApi

    @Binds
    @ScreenScope
    abstract fun bindsChatStateMapper(chatStateMapper: ChatStateMapper): StateMapper<DomainState, UIState>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @ScreenScope
        fun provideMessageGenerator(screen: BaseScreen<*, *, *>): MessageGenerator =
            MessageGeneratorImpl(screen.activity)
    }

}