package com.revolut.kompot.build_first_screen

import android.app.Application
import com.revolut.kompot.build_first_screen.di.AppComponent
import com.revolut.kompot.build_first_screen.di.DaggerAppComponent

class App: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.create()
    }

}