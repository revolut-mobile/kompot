package com.revolut.kompot.sample.feature.contacts.di

import com.revolut.kompot.sample.feature.contacts.data.ContactsRepository
import com.revolut.kompot.sample.feature.contacts.data.repository.ContactsRepositoryImpl
import com.revolut.kompot.sample.utils.di.FeatureScope
import dagger.Binds
import dagger.Module

@Module
abstract class ContactsFeatureModule {

    @Binds
    @FeatureScope
    abstract fun bindsContactsRepository(contactsRepository: ContactsRepositoryImpl): ContactsRepository

}