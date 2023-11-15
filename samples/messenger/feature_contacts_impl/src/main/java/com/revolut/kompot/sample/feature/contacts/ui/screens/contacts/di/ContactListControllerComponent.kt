package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di

import com.revolut.kompot.navigable.vc.di.ViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerScope
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListContract
import dagger.Subcomponent

@ViewControllerScope
@Subcomponent(
    modules = [ContactListControllerModule::class]
)
interface ContactListControllerComponent : ViewControllerComponent {
    val model: ContactListContract.ModelApi

    @Subcomponent.Builder
    interface Builder : ViewControllerComponent.Builder<ContactListControllerComponent, Builder>
}