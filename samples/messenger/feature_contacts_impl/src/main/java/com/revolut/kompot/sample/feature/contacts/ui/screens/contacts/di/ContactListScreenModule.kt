package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di

import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenModule
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract.*
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenModel
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListStateMapper
import dagger.Binds
import dagger.Module

@Module
abstract class ContactListScreenModule : BaseScreenModule {

    @Binds
    @ScreenScope
    abstract fun bindsStateMapper(contactListStateMapper: ContactListStateMapper): StateMapper<DomainState, UIState>

    @Binds
    @ScreenScope
    abstract fun bindsScreenModel(contactListScreenModel: ContactListScreenModel): ScreenModelApi

}