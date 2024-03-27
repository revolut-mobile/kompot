package com.revolut.kompot.sample.feature.chat.ui.screens.chat

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ui.list.ModelBinding
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesController
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.databinding.ScreenChatBinding
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.navigation.ChatNavigationDestination.InputData
import com.revolut.kompot.sample.feature.chat.ui.delegates.MessageRowDelegate
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.ChatContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.di.ChatControllerInjector

class ChatViewController(inputData: InputData) : ViewController<IOData.EmptyOutput>(),
    UIListStatesController<UIState> {

    override val layoutId: Int = R.layout.screen_chat
    private val binding by viewBinding(ScreenChatBinding::bind)
    private val messageRowDelegate = MessageRowDelegate()

    override val fitStatusBar: Boolean = true

    override val component = (ChatsApiProvider.component as ChatControllerInjector)
        .getChatControllerComponentBuilder()
        .inputData(inputData)
        .controller(this)
        .build()

    override val controllerModel = component.model
    override val modelBinding = ModelBinding(
        model = controllerModel,
        delegates = listOf(messageRowDelegate),
        debounceStreamProvider = { binding.vMessageInput.textStream() },
        layoutManagerProvider = { context ->
            LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    )

    override fun onShown(view: View) {
        binding.vMessageInput.textStream()
            .collectTillDetachView { text ->
                controllerModel.onInputChanged(text)
            }
        binding.vMessageInput.actionClicksStream()
            .collectTillDetachView {
                controllerModel.onActionButtonClick()
            }
        binding.vNavBar.navigationButtonClicksStream()
            .collectTillDetachView {
                activity.onBackPressed()
            }
        messageRowDelegate.observeItemClicksStream()
            .collectTillDetachView {
                controllerModel.onMessageClicked(it)
            }
    }

    override fun render(uiState: UIState, payload: Any?) {
        bindChatNavBar(uiState)
        bindMessageInput(uiState)
        if (uiState.items.isNotEmpty()) {
            modelBinding.recyclerView.scrollToPosition(uiState.items.size - 1)
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