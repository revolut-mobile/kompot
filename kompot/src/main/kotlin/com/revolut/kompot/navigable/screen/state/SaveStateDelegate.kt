package com.revolut.kompot.navigable.screen.state

import com.revolut.kompot.navigable.screen.ScreenStates

abstract class SaveStateDelegate<T : ScreenStates.Domain, R : ScreenStates.RetainedDomain> {

    abstract fun getRetainedState(currentState: T): R?

    abstract fun restoreDomainState(initialState: T, retainedState: R): T

    @Suppress("UNCHECKED_CAST")
    internal fun restoreDomainStateInternal(initialState: T, retainedState: Any): T =
        restoreDomainState(initialState, retainedState as R)

}