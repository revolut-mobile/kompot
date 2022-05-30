package com.revolut.kompot.sample.feature.chat.di

import com.revolut.kompot.sample.feature.chat.data.ChatRepository
import com.revolut.kompot.sample.feature.chat.data.repository.ChatRepositoryImpl
import com.revolut.kompot.sample.utils.di.FeatureScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class ChatFeatureModule {

    @Binds
    @FeatureScope
    abstract fun bindsChatsRepository(chatsRepository: ChatRepositoryImpl): ChatRepository

}