package com.revolut.kompot

import com.revolut.kompot.navigable.Controller
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

object KompotPlugin {
    internal val controllerShownSharedFlow = MutableSharedFlow<Controller>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun controllerShowingStream(): Flow<Controller> = controllerShownSharedFlow
}