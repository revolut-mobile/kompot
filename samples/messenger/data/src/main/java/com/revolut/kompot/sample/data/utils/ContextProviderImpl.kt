package com.revolut.kompot.sample.data.utils

import android.app.Application
import android.content.Context
import javax.inject.Inject

internal class ContextProviderImpl @Inject constructor(
    private val application: Application
) : ContextProvider {

    override fun provideContext(): Context = application

}