package com.revolut.kompot.build_first_screen.screen

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

internal class DemoScreenModel @Inject constructor(
    stateMapper: StateMapper<DemoScreenContract.DomainState, DemoScreenContract.UIState>,
    inputData: DemoScreenContract.InputData
) : BaseScreenModel<DemoScreenContract.DomainState, DemoScreenContract.UIState, IOData.EmptyOutput>(stateMapper),
    DemoScreenContract.ScreenModelApi {

    override val initialState = DemoScreenContract.DomainState(inputData.title)

}