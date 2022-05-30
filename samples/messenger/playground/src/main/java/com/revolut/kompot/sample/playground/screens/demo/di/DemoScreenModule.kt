package com.revolut.kompot.sample.playground.screens.demo.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenContract
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenModel
import com.revolut.kompot.sample.playground.screens.demo.DemoStateMapper
import dagger.Binds
import dagger.Module

@Module
internal abstract class DemoScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun provideMapper(mapper: DemoStateMapper): StateMapper<DemoScreenContract.DomainState, DemoScreenContract.UIState>

    @Binds
    @ScreenScope
    abstract fun provideScreenModel(model: DemoScreenModel): DemoScreenContract.ScreenModelApi
}