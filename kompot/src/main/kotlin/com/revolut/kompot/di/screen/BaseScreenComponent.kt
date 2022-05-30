package com.revolut.kompot.di.screen

import com.revolut.kompot.navigable.screen.ScreenExtensionsInjector
import com.revolut.kompot.navigable.screen.BaseScreen
import dagger.BindsInstance

interface BaseScreenComponent : ScreenExtensionsInjector {
    interface Builder<T : BaseScreenComponent, B> {

        @BindsInstance
        fun screen(screen: BaseScreen<*, *, *>): B

        fun build(): T

    }
}