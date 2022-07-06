package com.revolut.kompot.build_first_screen.screen.di

import com.revolut.kompot.build_first_screen.screen.DemoScreenContract
import com.revolut.kompot.build_first_screen.screen.DemoScreenContract.InputData
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
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
        fun inputData(inputData: InputData): Builder
    }
}