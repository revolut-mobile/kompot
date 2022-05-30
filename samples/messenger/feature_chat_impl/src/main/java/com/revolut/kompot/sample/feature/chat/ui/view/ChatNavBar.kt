package com.revolut.kompot.sample.feature.chat.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.appbar.AppBarLayout
import com.revolut.kompot.sample.feature.chat.databinding.ViewChatNavBarBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ChatNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppBarLayout(context, attrs) {

    private val binding = ViewChatNavBarBinding.inflate(LayoutInflater.from(context), this, true)

    private val onNavigationButtonClickSharedFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }

    init {
        binding.btnNavigation.setOnClickListener {
            onNavigationButtonClickSharedFlow.tryEmit(Unit)
        }
    }

    fun setTitle(text: String) {
        if (binding.tvTitle.text != text) {
            binding.tvTitle.text = text
        }
    }

    fun setSubtitle(text: String) {
        if (binding.tvSubtitle.text != text) {
            binding.tvSubtitle.text = text
        }
    }

    fun setIcon(resId: Int) {
        binding.ivIcon.setImageResource(resId)
    }

    fun navigationButtonClicksStream(): Flow<Unit> = onNavigationButtonClickSharedFlow

}