package com.revolut.kompot.sample.playground.screens.demo

import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.playground.delegates.ButtonDelegate
import com.revolut.kompot.sample.playground.delegates.RowDelegate
import javax.inject.Inject

class DemoStateMapper @Inject constructor(
    private val inputData: DemoScreenContract.InputData
) : StateMapper<DemoScreenContract.DomainState, DemoScreenContract.UIState> {

    override fun mapState(domainState: DemoScreenContract.DomainState): DemoScreenContract.UIState {
        return DemoScreenContract.UIState(
            items = listOf(
                RowDelegate.Model(
                    listId = "",
                    title = inputData.title,
                    subtitle = "Count ${inputData.counter}"
                ),
                ButtonDelegate.Model(
                    listId = "action",
                    text = "Action"
                ),
                ButtonDelegate.Model(
                    listId = "result",
                    text = "Post result"
                )
            )
        )
    }
}