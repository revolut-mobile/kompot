package com.revolut.kompot.build_first_flow.screen.input.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.revolut.kompot.build_first_flow.databinding.ViewInputBinding
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class InputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewInputBinding.inflate(LayoutInflater.from(context), this, true)

    private val textSharedFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_LATEST
        )
    }

    private val textWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textSharedFlow.tryEmit(s.toString())
        }

    }

    init {
        binding.edInput.addTextChangedListener(textWatcher)
    }

    fun textFlow(): Flow<String> = textSharedFlow

    fun setInputHint(hint: String) {
        if (binding.inputLayout.hint != hint) {
            binding.inputLayout.hint = hint
        }
    }

    fun setInputText(text: String) {
        if (binding.edInput.text.toString() != text) {
            binding.edInput.setText(text)
        }
    }

}