package com.revolut.kompot.build_first_flow.screen.text.di

import com.revolut.kompot.build_first_flow.screen.text.TextScreenContract
import com.revolut.kompot.build_first_flow.screen.text.TextScreenContract.InputData
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(
    modules = [TextScreenModule::class]
)
interface TextScreenComponent : BaseScreenComponent {

    val screenModel: TextScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<TextScreenComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: InputData): Builder
    }
}