package com.revolut.kompot.sample.feature.chat.ui.screens.chat.di

import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract
import dagger.BindsInstance
import dagger.Subcomponent

@ViewControllerScope
@Subcomponent(modules = [ChatControllerModule::class])
interface ChatControllerComponent : ViewControllerComponent {

    val model: ChatContract.ModelApi

    @Subcomponent.Builder
    interface Builder : ViewControllerComponent.Builder<ChatControllerComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: ChatNavigationDestination.InputData): Builder
    }
}