package com.revolut.kompot.navigable.screen

interface StateMapper<IN : ScreenStates.Domain, OUT : ScreenStates.UI> {
    fun mapState(domainState: IN): OUT
}