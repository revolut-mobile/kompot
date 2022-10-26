package com.revolut.kompot.sample.di

import com.revolut.kompot.FeaturesRegistry
import com.revolut.kompot.sample.SampleApplication
import com.revolut.kompot.sample.ui.flows.main.di.MainFlowComponent
import com.revolut.kompot.sample.ui.flows.root.di.RootFlowComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppModule::class]
)
interface AppComponent {
    val rootFlowComponent: RootFlowComponent.Builder

    val mainFlowComponent: MainFlowComponent.Builder

    val featureRegistry: FeaturesRegistry

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun sampleApplication(app: SampleApplication): Builder
        fun build(): AppComponent
    }

}