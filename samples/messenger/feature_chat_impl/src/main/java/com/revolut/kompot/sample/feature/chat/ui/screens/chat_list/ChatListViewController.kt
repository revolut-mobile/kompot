package com.revolut.kompot.sample.feature.chat.ui.screens.chat_list

import android.view.View
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ui.list.ModelBinding
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesController
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.kompot.sample.feature.chat.di.ChatsApiProvider
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.OutputData
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.ChatListContract.UIState
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di.ChatListControllerInjector
import com.revolut.kompot.sample.feature.contacts.domain.Contact
import com.revolut.kompot.sample.ui_common.RowDelegate

class ChatListViewController : ViewController<OutputData>(), UIListStatesController<UIState> {

    override val layoutId: Int = R.layout.screen_chat_list
    override val fitStatusBar: Boolean = true
    private val chatRowDelegate = RowDelegate()

    override val component by lazy(LazyThreadSafetyMode.NONE) {
        (ChatsApiProvider.component as ChatListControllerInjector)
            .getChatListComponentBuilder()
            .controller(this)
            .build()
    }

    override val controllerModel by lazy(LazyThreadSafetyMode.NONE) {
        component.model
    }
    override val modelBinding by lazy(LazyThreadSafetyMode.NONE) {
        ModelBinding(
            model = controllerModel,
            delegates = listOf(chatRowDelegate)
        )
    }

    override fun onShown(view: View) {
        chatRowDelegate.clicksFlow()
            .collectTillDetachView { model ->
                controllerModel.onRowClicked(model.parcel as Contact)
            }
    }
}