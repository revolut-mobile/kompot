package com.revolut.kompot.sample.playground.screens.demo

import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.StateMapper
import javax.inject.Inject

internal class DemoScreenModel @Inject constructor(
    stateMapper: StateMapper<DemoScreenContract.DomainState, DemoScreenContract.UIState>,
    private val inputData: DemoScreenContract.InputData
) : BaseScreenModel<DemoScreenContract.DomainState, DemoScreenContract.UIState, DemoScreenContract.OutputData>(stateMapper),
    DemoScreenContract.ScreenModelApi {

    override val initialState = DemoScreenContract.DomainState(0)

    override fun onAction(id: String) {
        when (id) {
            "action" -> DemoScreen(
                inputData = DemoScreenContract.InputData(
                    inputData.title, inputData.counter + 1
                )
            ).showModal()
            "result" -> postScreenResult(DemoScreenContract.OutputData(0))
        }
    }
}