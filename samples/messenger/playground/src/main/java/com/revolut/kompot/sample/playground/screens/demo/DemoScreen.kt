package com.revolut.kompot.sample.playground.screens.demo

import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.revolut.kompot.navigable.screen.BaseRecyclerViewScreen
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.sample.playground.R
import com.revolut.kompot.sample.playground.delegates.ButtonDelegate
import com.revolut.kompot.sample.playground.delegates.RowDelegate
import com.revolut.kompot.sample.playground.di.PlaygroundApiProvider
import com.revolut.kompot.sample.playground.screens.demo.DemoScreenContract.*
import com.revolut.kompot.sample.playground.screens.demo.di.DemoScreenInjector

class DemoScreen(inputData: InputData) : BaseRecyclerViewScreen<UIState, InputData, OutputData>(inputData) {

    override val screenComponent by lazy(LazyThreadSafetyMode.NONE) {
        PlaygroundApiProvider.component
            .getDemoScreenComponentBuilder()
            .screen(this)
            .inputData(inputData)
            .build()
    }

    override val layoutId = R.layout.screen_demo

    override val screenModel by lazy(LazyThreadSafetyMode.NONE) { screenComponent.screenModel }

    private val buttonDelegate = ButtonDelegate()

    override val delegates = listOf(
        RowDelegate(),
        buttonDelegate
    )

    override val fitStatusBar = true

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        buttonDelegate.clicksFlow().collectTillDetachView { model ->
            screenModel.onAction(model.listId)
        }
    }

    override fun bindScreen(uiState: UIState, payload: ScreenStates.UIPayload?) {
        super.bindScreen(uiState, payload)
        view.findViewById<FrameLayout>(R.id.mainLayout).background = ColorDrawable(inputData.color)
    }
}