package com.revolut.kompot.sample.feature.chat.di

import com.revolut.kompot.FeatureInitialisationArgs
import com.revolut.kompot.sample.data.api.DataApi
import com.revolut.kompot.sample.feature.chat.api.ChatApi
import com.revolut.kompot.sample.feature.chat.ui.screens.chat.di.ChatScreenInjector
import com.revolut.kompot.sample.feature.chat.ui.screens.chat_list.di.ChatListScreenInjector
import com.revolut.kompot.sample.feature.contacts.api.ContactsApi
import com.revolut.kompot.sample.utils.api.UtilsApi
import com.revolut.kompot.sample.utils.LazySingletonHolder
import com.revolut.kompot.sample.utils.di.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    modules = [ChatFeatureModule::class],
    dependencies = [DataApi::class, UtilsApi::class, ContactsApi::class]
)
interface ChatFeatureComponent : ChatApi, ChatScreenInjector, ChatListScreenInjector {
    @Component.Factory
    interface Builder {
        fun create(
            dataApi: DataApi,
            coreUtilsApi: UtilsApi,
            contactsApi: ContactsApi
        ) : ChatFeatureComponent
    }
}

data class ChatArguments(
    val dataApi: DataApi,
    val coreUtilsApi: UtilsApi,
    val contactsApi: ContactsApi
) : FeatureInitialisationArgs

class ChatsApiProvider {

    companion object : LazySingletonHolder<ChatApi, ChatArguments>({ args ->
        DaggerChatFeatureComponent.factory().create(
            dataApi = args.dataApi,
            coreUtilsApi = args.coreUtilsApi,
            contactsApi = args.contactsApi
        )
    }) {

        internal val component: ChatFeatureComponent get() = instance as ChatFeatureComponent

    }

}