package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di

import com.revolut.kompot.navigable.vc.di.ViewControllerModule
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.DomainState
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.ModelApi
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract.UIState
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListStateMapper
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListViewModel
import dagger.Binds
import dagger.Module

@Module
abstract class ContactListControllerModule : ViewControllerModule {

    @[Binds ViewControllerScope]
    abstract fun bindsStateMapper(contactListStateMapper: ContactListStateMapper): States.Mapper<DomainState, UIState>

    @[Binds ViewControllerScope]
    abstract fun bindsScreenModel(contactListScreenModel: ContactListViewModel): ModelApi
}