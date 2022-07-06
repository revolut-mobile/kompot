package com.revolut.kompot.sample.feature.contacts.ui.screens.input.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.InputData
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(
    modules = [InputScreenModule::class]
)
interface InputScreenComponent : BaseScreenComponent {

    val screenModel: InputScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<InputScreenComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: InputData): Builder
    }
}