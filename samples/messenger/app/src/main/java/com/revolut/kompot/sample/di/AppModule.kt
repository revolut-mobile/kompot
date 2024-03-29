package com.revolut.kompot.sample.di

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import com.revolut.kompot.DefaultFeaturesRegistry
import com.revolut.kompot.FeaturesRegistry
import com.revolut.kompot.di.ThemedApplicationContext
import com.revolut.kompot.sample.Features
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.SampleApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        @ThemedApplicationContext
        fun provideThemedApplicationContext(application: SampleApplication): Context =
            ContextThemeWrapper(application, R.style.AppTheme)

        @[Provides Singleton]
        fun provideFeaturesRegistry(): FeaturesRegistry {
            val featuresRegistry = DefaultFeaturesRegistry()
            featuresRegistry.registerFeatures(Features.createFeaturesList())
            return featuresRegistry
        }
    }
}