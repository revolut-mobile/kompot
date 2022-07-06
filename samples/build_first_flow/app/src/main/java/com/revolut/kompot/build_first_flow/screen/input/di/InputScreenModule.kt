package com.revolut.kompot.build_first_flow.screen.input.di

import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.DomainState
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.UIState
import com.revolut.kompot.build_first_flow.screen.input.InputScreenModel
import com.revolut.kompot.build_first_flow.screen.input.InputStateMapper
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
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