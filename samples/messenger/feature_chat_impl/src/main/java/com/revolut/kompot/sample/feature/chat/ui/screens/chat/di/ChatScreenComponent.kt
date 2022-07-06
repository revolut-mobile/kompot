package com.revolut.kompot.sample.feature.chat.ui.screens.chat.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(modules = [ChatScreenModule::class])
interface ChatScreenComponent : BaseScreenComponent {

    val screenModel: ChatScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<ChatScreenComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: ChatNavigationDestination.InputData): Builder
    }

}