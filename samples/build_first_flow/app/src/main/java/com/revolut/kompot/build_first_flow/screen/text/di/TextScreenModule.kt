package com.revolut.kompot.build_first_flow.screen.text.di

import com.revolut.kompot.build_first_flow.screen.text.TextScreenContract
import com.revolut.kompot.build_first_flow.screen.text.TextScreenContract.DomainState
import com.revolut.kompot.build_first_flow.screen.text.TextScreenContract.UIState
import com.revolut.kompot.build_first_flow.screen.text.TextScreenModel
import com.revolut.kompot.build_first_flow.screen.text.TextStateMapper
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
import dagger.Binds
import dagger.Module

@Module
internal abstract class TextScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun provideMapper(mapper: TextStateMapper): StateMapper<DomainState, UIState>

    @Binds
    @ScreenScope
    abstract fun provideScreenModel(model: TextScreenModel): TextScreenContract.ScreenModelApi
}