package com.revolut.kompot.build_first_flow.screen.text

import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

class TextStateMapper @Inject constructor() :
    StateMapper<TextScreenContract.DomainState, TextScreenContract.UIState> {

    override fun mapState(domainState: TextScreenContract.DomainState) =
        TextScreenContract.UIState(domainState.text)
}