package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.scope.ScreenScope
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.ContactListScreenContract
import dagger.BindsInstance
import dagger.Subcomponent

@ScreenScope
@Subcomponent(
    modules = [ContactListScreenModule::class]
)
interface ContactListScreenComponent : BaseScreenComponent {
    val screenModel: ContactListScreenContract.ScreenModelApi

    @Subcomponent.Builder
    interface Builder : BaseScreenComponent.Builder<ContactListScreenComponent, Builder> {
        @BindsInstance
        fun inputData(ioData: IOData.EmptyInput): Builder
    }

}