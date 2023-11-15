package com.revolut.kompot.sample.feature.contacts.ui.screens.input.di

import com.revolut.kompot.navigable.vc.di.ViewControllerModule
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.UIState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputStateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputViewModel
import dagger.Binds
import dagger.Module

@Module
internal abstract class InputControllerModule : ViewControllerModule {

    @[Binds ViewControllerScope]
    abstract fun provideMapper(mapper: InputStateMapper): States.Mapper<DomainState, UIState>

    @[Binds ViewControllerScope]
    abstract fun provideViewModel(model: InputViewModel): InputContract.ModelApi
}