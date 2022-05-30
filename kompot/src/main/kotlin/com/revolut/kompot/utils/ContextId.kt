package com.revolut.kompot.utils

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

sealed class ContextId: AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ContextId>

    object CreatedScopeContextId: ContextId()
    object ShownScopeContextId: ContextId()
}