package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import android.view.View
import com.revolut.kompot.navigable.screen.BaseScreen
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.databinding.ScreenInputBinding
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputScreenContract.*
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.di.InputScreenInjector
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