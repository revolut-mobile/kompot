package com.revolut.kompot.sample.feature.chat.ui.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.databinding.ViewMessageInputBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class MessageInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val binding = ViewMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

    private val onActionSharedFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }
    private val textSharedFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableSharedFlow<String>(extraBufferCapacity = 1)
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
        elevation = context.resources.getDimension(R.dimen.elevation)

        binding.edMsgInput.addTextChangedListener(textWatcher)
        binding.btnSend.setOnClickListener { onActionSharedFlow.tryEmit(Unit) }
    }

    fun textStream(): Flow<String> = textSharedFlow
    fun actionClicksStream(): Flow<Unit> = onActionSharedFlow

    fun setInputText(text: String) {
        if (binding.edMsgInput.text.toString() != text) {
            binding.edMsgInput.setText(text)
        }
    }

    fun setActionButtonEnabled(enabled: Boolean) {
        binding.btnSend.isEnabled = enabled
    }

}