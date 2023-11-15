package com.revolut.kompot.sample.playground.di

import com.revolut.kompot.sample.playground.PlaygroundApi
import com.revolut.kompot.sample.playground.flows.demo.di.DemoFlowInjector
import com.revolut.kompot.sample.playground.flows.scroller.di.DemoScrollerFlowInjector
import com.revolut.kompot.sample.playground.screens.demo.di.DemoScreenInjector
import com.revolut.kompot.sample.utils.LazySingletonHolder
import com.revolut.kompot.sample.utils.di.FeatureScope
import dagger.Component

@FeatureScope
@Component
interface PlaygroundFeatureComponent : PlaygroundApi, DemoFlowInjector, DemoScreenInjector,
    DemoScrollerFlowInjector {
    @Component.Factory
    interface Builder {
        fun create(): PlaygroundFeatureComponent
    }
}

object PlaygroundArguments

class PlaygroundApiProvider {

    companion object : LazySingletonHolder<PlaygroundApi, PlaygroundArguments>({
        DaggerPlaygroundFeatureComponent.factory().create()
    }) {

        internal val component: PlaygroundFeatureComponent get() = instance as PlaygroundFeatureComponent

    }

}