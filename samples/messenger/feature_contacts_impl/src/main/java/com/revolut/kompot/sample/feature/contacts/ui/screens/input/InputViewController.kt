package com.revolut.kompot.sample.feature.contacts.ui.screens.input

import android.view.View
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ui.ModelBinding
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.sample.feature.contacts.R
import com.revolut.kompot.sample.feature.contacts.databinding.ScreenInputBinding
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.InputContract.*
import com.revolut.kompot.sample.feature.contacts.ui.screens.input.di.InputControllerInjector

class InputViewController(inputType: InputType) : ViewController<OutputData>(), UIStatesController<UIState> {

    override val layoutId = R.layout.screen_input
    private val binding by viewBinding(ScreenInputBinding::bind)

    override val fitStatusBar = true
    override val component by lazy(LazyThreadSafetyMode.NONE) {
        (parentComponent as InputControllerInjector)
            .getInputComponentBuilder()
            .controller(this)
            .inputType(inputType)
            .build()
    }
    override val controllerModel by lazy(LazyThreadSafetyMode.NONE) { component.model }
    override val modelBinding by lazy(LazyThreadSafetyMode.NONE) {
        ModelBinding(controllerModel)
    }

    override fun onShown(view: View) {
        binding.vInput.textFlow()
            .collectTillDetachView { text ->
                controllerModel.onInputChanged(text)
            }
        binding.btnContinue.setOnClickListener {
            controllerModel.onActionClick()
        }
    }

    override fun render(uiState: UIState, payload: Any?) {
        binding.vInput.setInputHint(uiState.inputHint)
        binding.vInput.setInputText(uiState.inputText)
    }
}