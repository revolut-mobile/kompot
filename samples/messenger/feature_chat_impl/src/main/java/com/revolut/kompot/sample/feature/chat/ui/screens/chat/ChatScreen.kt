package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseRecyclerViewScreen
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.databinding.ScreenChatBinding
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination.InputData
import com.revolut.kompot.sample.feature.chat.ui.delegates.MessageRowDelegate
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatScreenContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.di.ChatScreenComponent
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.di.ChatScreenInjector
import com.revolut.recyclerkit.delegates.RecyclerViewDelegate
import kotlinx.coroutines.flow.Flow

class ChatScreen(inputData: InputData) : BaseRecyclerViewScreen<UIState, InputData, IOData.EmptyOutput>(inputData) {

    override val layoutId: Int = R.layout.screen_chat
    private val binding by viewBinding(ScreenChatBinding::bind)

    override val fitStatusBar: Boolean = true

    override val screenComponent: ChatScreenComponent by lazy(LazyThreadSafetyMode.NONE) {
        (ChatsApiProvider.component as ChatScreenInjector)
            .getChatScreenComponentBuilder()
            .inputData(inputData)
            .screen(this)
            .build()
    }

    override val screenModel by lazy(LazyThreadSafetyMode.NONE) {
        screenComponent.screenModel
    }

    override val delegates: List<RecyclerViewDelegate<*, *>>
        get() = listOf(
            MessageRowDelegate()
        )

    override fun debounceStream(): Flow<Any> =
        binding.vMessageInput.textStream()

    override fun createLayoutManager(context: Context): RecyclerView.LayoutManager {
        return LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
    }

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        binding.vMessageInput.textStream()
            .collectTillDetachView { text ->
                screenModel.onInputChanged(text)
            }

        binding.vMessageInput.actionClicksStream()
            .collectTillDetachView {
                screenModel.onActionButtonClick()
            }
        binding.vNavBar.navigationButtonClicksStream()
            .collectTillDetachView {
                activity.onBackPressed()
            }
    }

    override fun bindScreen(uiState: UIState, payload: ScreenStates.UIPayload?) {
        super.bindScreen(uiState, payload)

        bindChatNavBar(uiState)
        bindMessageInput(uiState)
        if (uiState.items.isNotEmpty()) {
            recyclerView.scrollToPosition(uiState.items.size - 1)
        }
    }

    private fun bindChatNavBar(uiState: UIState) = with(binding.vNavBar) {
        setTitle(uiState.contactName)
        setSubtitle(uiState.contactStatus)
        setIcon(uiState.contactAvatar)
    }

    private fun bindMessageInput(uiState: UIState) = with(binding.vMessageInput) {
        setInputText(uiState.messageInputText)
        setActionButtonEnabled(uiState.actionButtonEnabled)
    }

}