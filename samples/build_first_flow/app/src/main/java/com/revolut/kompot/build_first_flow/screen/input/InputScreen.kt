package com.revolut.kompot.build_first_flow.screen.input

import android.view.View
import com.revolut.kompot.build_first_flow.R
import com.revolut.kompot.build_first_flow.databinding.ScreenInputBinding
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.InputData
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.OutputData
import com.revolut.kompot.build_first_flow.screen.input.InputScreenContract.UIState
import com.revolut.kompot.build_first_flow.screen.input.di.InputScreenInjector
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.utils.viewBinding

class InputScreen(inputData: InputData) : BaseScreen<UIState, InputData, OutputData>(inputData) {

    override val layoutId = R.layout.screen_input
    private val binding by viewBinding(ScreenInputBinding::bind)

    override val screenComponent by lazy(LazyThreadSafetyMode.NONE) {
        (flowComponent as InputScreenInjector)
            .getInputScreenComponentBuilder()
            .screen(this)
            .inputData(inputData)
            .build()
    }

    override val screenModel by lazy(LazyThreadSafetyMode.NONE) { screenComponent.screenModel }

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        binding.vInput.textFlow()
            .collectTillDetachView { text ->
                screenModel.onInputChanged(text)
            }

        binding.btnContinue.setOnClickListener {
            screenModel.onActionClick()
        }
    }

    override fun bindScreen(uiState: UIState, payload: ScreenStates.UIPayload?) {
        binding.vInput.setInputHint(uiState.inputHint)
        binding.vInput.setInputText(uiState.inputText)
    }
}