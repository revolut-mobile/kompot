package com.revolut.kompot.sample.feature.contacts.ui.screens.input.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.UIState
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenModel
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputStateMapper
import dagger.Binds
import dagger.Module

@Module
internal abstract class InputScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun provideMapper(mapper: InputStateMapper): StateMapper<DomainState, UIState>

    @Binds
    @ScreenScope
    abstract fun provideScreenModel(model: InputScreenModel): InputScreenContract.ScreenModelApi
}