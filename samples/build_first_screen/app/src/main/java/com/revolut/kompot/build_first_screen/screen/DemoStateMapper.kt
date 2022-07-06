package com.revolut.kompot.build_first_screen.screen

import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

class DemoStateMapper @Inject constructor() :
    StateMapper<DemoScreenContract.DomainState, DemoScreenContract.UIState> {

    override fun mapState(domainState: DemoScreenContract.DomainState) =
        DemoScreenContract.UIState(domainState.title)
}