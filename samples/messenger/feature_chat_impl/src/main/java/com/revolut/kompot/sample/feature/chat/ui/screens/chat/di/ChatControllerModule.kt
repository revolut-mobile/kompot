package com.revolut.kompot.sample.feature.chat.ui.screens.chat.di

import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModelExtension
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.di.ViewControllerModule
import com.revolut.kompot.navigable.vc.di.ViewControllerQualifier
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.DomainState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.ModelApi
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatStateMapper
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatViewModel
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.extension.ChatMessageActionExtension
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.extension.ChatMessageActionExtensionImpl
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGenerator
import com.revolut.kompot.sample.feature.chat.utils.message_generator.MessageGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
abstract class ChatControllerModule : ViewControllerModule {

    @[Binds ViewControllerScope]
    abstract fun bindsChatViewModel(chatViewModel: ChatViewModel): ModelApi

    @[Binds ViewControllerScope]
    abstract fun bindsChatStateMapper(chatStateMapper: ChatStateMapper): States.Mapper<DomainState, UIState>

    @[Binds ViewControllerScope]
    abstract fun bindsChatMessageActionExtension(delegate: ChatMessageActionExtensionImpl): ChatMessageActionExtension

    @Module
    companion object {
        @[Provides JvmStatic ViewControllerScope]
        fun provideMessageGenerator(@ViewControllerQualifier controller: ViewController<*>): MessageGenerator =
            MessageGeneratorImpl(
                contextProvider = { controller.activity }
            )

        @[Provides IntoSet ViewControllerScope]
        fun bindChatMessageActionDelegateAsControllerModelExtension(extension: ChatMessageActionExtension): ControllerModelExtension {
            return extension as ControllerModelExtension
        }
    }

}