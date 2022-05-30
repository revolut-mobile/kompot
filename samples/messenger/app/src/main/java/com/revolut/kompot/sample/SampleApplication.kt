package com.revolut.kompot.sample

import android.app.Application
import android.content.Context
import com.revolut.kompot.sample.utils.di.CoreUtilsApiProvider
import com.revolut.kompot.sample.data.di.DataApiProvider
import com.revolut.kompot.sample.data.di.DataArguments
import com.revolut.kompot.sample.di.AppComponent
import com.revolut.kompot.sample.di.DaggerAppComponent
import timber.log.Timber

class SampleApplication : Application() {
    val appComponent: AppComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerAppComponent.builder()
            .sampleApplication(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        DataApiProvider.init {
            DataArguments(
                application = this
            )
        }
        CoreUtilsApiProvider.init {

        }
    }

}

val Context.sampleApplication
    get() = this.applicationContext as SampleApplication