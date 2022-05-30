package com.revolut.kompot.di.screen

import com.revolut.kompot.di.scope.ScreenQualifier
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.screen.BaseScreen
import dagger.Binds
import dagger.multibindings.Multibinds

interface BaseScreenModule {
    @Binds
    @ScreenScope
    @ScreenQualifier
    fun provideController(screen: BaseScreen<*, *, *>): Controller

    @Multibinds
    @ScreenScope
    fun provideControllerExtensions(): Set<ControllerExtension>
}