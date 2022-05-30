package com.revolut.kompot.sample.di

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import com.revolut.kompot.FeaturesManager
import com.revolut.kompot.FeaturesManagerImpl
import com.revolut.kompot.di.ThemedApplicationContext
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.SampleApplication
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun provideFeaturesManager(manager: FeaturesManagerImpl): FeaturesManager

    @Module
    companion object {

        @JvmStatic
        @Provides
        @Singleton
        @ThemedApplicationContext
        fun provideThemedApplicationContext(application: SampleApplication): Context = ContextThemeWrapper(application, R.style.AppTheme)
    }
}