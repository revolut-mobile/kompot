package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di

import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.di.ViewControllerModule
import com.revolut.kompot.navigable.vc.di.ViewControllerQualifier
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.ModelApi
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListStateMapper
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListViewModel
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class ChatListControllerModule : ViewControllerModule {

    @[Binds ViewControllerScope]
    abstract fun bindMapper(mapper: ChatListStateMapper): States.Mapper<DomainState, UIState>

    @[Binds ViewControllerScope]
    abstract fun bindModel(model: ChatListViewModel): ModelApi

    @Module
    companion object {
        @[Provides JvmStatic ViewControllerScope]
        fun provideMessageGenerator(@ViewControllerQualifier controller: ViewController<*>): MessageGenerator =
            MessageGeneratorImpl(
                contextProvider = { controller.activity }
            )
    }
}