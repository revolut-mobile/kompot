package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseRecyclerViewScreen
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenContract.OutputData
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListScreenContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di.ChatListScreenComponent
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di.ChatListScreenInjector
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.ui_common.RowDelegate

class ChatListScreen : BaseRecyclerViewScreen<UIState, IOData.EmptyInput, OutputData>(IOData.EmptyInput) {

    override val layoutId: Int = R.layout.screen_chat_list

    override val fitStatusBar: Boolean = true

    private val chatRowDelegate = RowDelegate()

    override val delegates = listOf(chatRowDelegate)

    override val screenComponent: ChatListScreenComponent by lazy(LazyThreadSafetyMode.NONE) {
        (ChatsApiProvider.component as ChatListScreenInjector)
            .getChatListScreenComponentBuilder()
            .inputData(inputData)
            .screen(this)
            .build()
    }

    override val screenModel by lazy {
        screenComponent.screenModel
    }

    override fun onScreenViewAttached(view: View) {
        super.onScreenViewAttached(view)

        chatRowDelegate.clicksFlow()
            .collectTillDetachView { model ->
                screenModel.onRowClicked(model.parcel as Contact)
            }
    }

}