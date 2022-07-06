package com.revolut.kompot.build_first_screen.screen.di

import com.revolut.kompot.build_first_screen.screen.DemoScreenContract
import com.revolut.kompot.build_first_screen.screen.DemoScreenContract.DomainState
import com.revolut.kompot.build_first_screen.screen.DemoScreenContract.UIState
import com.revolut.kompot.build_first_screen.screen.DemoScreenModel
import com.revolut.kompot.build_first_screen.screen.DemoStateMapper
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
import dagger.Binds
import dagger.Module

@Module
internal abstract class DemoScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun provideMapper(mapper: DemoStateMapper): StateMapper<DomainState, UIState>

    @Binds
    @ScreenScope
    abstract fun provideScreenModel(model: DemoScreenModel): DemoScreenContract.ScreenModelApi
}