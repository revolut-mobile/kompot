package com.revolut.kompot.sample.feature.contacts.ui.screens.input.di

import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract
import dagger.BindsInstance
import dagger.Subcomponent

@ViewControllerScope
@Subcomponent(
    modules = [InputControllerModule::class]
)
interface InputControllerComponent : ViewControllerComponent {

    val model: InputContract.ModelApi

    @Subcomponent.Builder
    interface Builder : ViewControllerComponent.Builder<InputControllerComponent, Builder> {
        @BindsInstance
        fun inputType(inputType: InputContract.InputType): Builder
    }
}