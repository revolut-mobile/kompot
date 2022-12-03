package com.revolut.kompot.build_first_screen.screen

import android.widget.TextView
import com.revolut.kompot.build_first_screen.R
import com.revolut.kompot.build_first_screen.flow.di.RootFlowComponent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenStates

class DemoScreen(title: String) :
    BaseScreen<DemoScreenContract.UIState, DemoScreenContract.InputData, IOData.EmptyOutput>(
        DemoScreenContract.InputData(title)
    ) {

    override val layoutId = R.layout.screen_demo
    override val fitStatusBar: Boolean = true

    override val screenComponent by lazy(LazyThreadSafetyMode.NONE) {
        (flowComponent as RootFlowComponent)
            .getDemoScreenComponentBuilder()
            .screen(this)
            .inputData(inputData)
            .build()
    }

    override val screenModel by lazy(LazyThreadSafetyMode.NONE) {
        screenComponent.screenModel
    }

    override fun bindScreen(uiState: DemoScreenContract.UIState, payload: ScreenStates.UIPayload?) {
        view.findViewById<TextView>(R.id.textView).text = uiState.title
    }
}