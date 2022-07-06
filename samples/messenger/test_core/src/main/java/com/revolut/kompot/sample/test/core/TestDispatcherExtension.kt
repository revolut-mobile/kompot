package com.revolut.kompot.sample.test.core

import android.annotation.SuppressLint
import com.revolut.kompot.coroutines.test.TestDispatchersHolder
import com.revolut.kompot.sample.utils.AppDispatchersPlugins
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherExtension : BeforeAllCallback, AfterAllCallback {

    val defaultDispatcher = TestDispatchersHolder.unconfinedTestDispatcher
    val ioDispatcher = TestDispatchersHolder.unconfinedTestDispatcher

    @SuppressLint("VisibleForTests")
    override fun beforeAll(context: ExtensionContext?) {
        AppDispatchersPlugins.setDefault(defaultDispatcher)
        AppDispatchersPlugins.setIo(ioDispatcher)
    }

    @SuppressLint("VisibleForTests")
    override fun afterAll(context: ExtensionContext?) {
        AppDispatchersPlugins.reset()
    }
}