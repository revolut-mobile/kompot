package com.revolut.kompot.build_first_flow.screen.text

import android.widget.TextView
import com.revolut.kompot.build_first_flow.R
import com.revolut.kompot.build_first_flow.flow.di.AddContactFlowComponent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenStates

class TextScreen(title: String) :
    BaseScreen<TextScreenContract.UIState, TextScreenContract.InputData, IOData.EmptyOutput>(
        TextScreenContract.InputData(title)
    ) {

    override val layoutId = R.layout.screen_text

    override val screenComponent by lazy(LazyThreadSafetyMode.NONE) {
        (flowComponent as AddContactFlowComponent)
            .getTextScreenComponentBuilder()
            .screen(this)
            .inputData(inputData)
            .build()
    }

    override val screenModel by lazy(LazyThreadSafetyMode.NONE) {
        screenComponent.screenModel
    }

    override fun bindScreen(uiState: TextScreenContract.UIState, payload: ScreenStates.UIPayload?) {
        view.findViewById<TextView>(R.id.textView).text = uiState.text
    }
}