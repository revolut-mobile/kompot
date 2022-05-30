package com.revolut.kompot.sample.playground.screens.demo.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenContract
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(
    modules = [DemoScreenModule::class]
)
interface DemoScreenComponent : BaseScreenComponent {

    val screenModel: DemoScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<DemoScreenComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: DemoScreenContract.InputData): Builder
    }
}