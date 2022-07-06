package com.revolut.kompot.build_first_flow.screen.input.di

import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.InputData
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
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