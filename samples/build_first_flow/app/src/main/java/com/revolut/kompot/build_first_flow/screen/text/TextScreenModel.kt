package com.revolut.kompot.build_first_flow.screen.text

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

internal class TextScreenModel @Inject constructor(
    stateMapper: StateMapper<TextScreenContract.DomainState, TextScreenContract.UIState>,
    inputData: TextScreenContract.InputData
) : BaseScreenModel<TextScreenContract.DomainState, TextScreenContract.UIState, IOData.EmptyOutput>(stateMapper),
    TextScreenContract.ScreenModelApi {

    override val initialState = TextScreenContract.DomainState(inputData.text)

}