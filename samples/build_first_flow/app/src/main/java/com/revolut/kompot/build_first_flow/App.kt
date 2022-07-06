package com.revolut.kompot.build_first_flow

import android.app.Application
import com.revolut.kompot.build_first_flow.di.AppComponent
import com.revolut.kompot.build_first_flow.di.DaggerAppComponent

class App: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.create()
    }

}