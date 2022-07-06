package com.revolut.kompot.sample.data.di

import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.data.database.ChatDatabase
import com.revolut.kompot.sample.data.utils.ContextProvider
import com.revolut.kompot.sample.data.utils.ContextProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun provideContext(contextProviderImpl: ContextProviderImpl): ContextProvider

    @Module
    companion object {
        @Provides
        @JvmStatic
        @Singleton
        fun provideChatDatabase(contextProvider: ContextProvider): ChatDatabase =
            ChatDatabase.build(contextProvider.provideContext())

        @Provides
        @JvmStatic
        @Singleton
        fun provideChatDao(chatDatabase: ChatDatabase): ChatDao = chatDatabase.chatDao
    }

}