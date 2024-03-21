package com.revolut.kompot.sample.feature.contacts.ui.screens.contacts.di

interface ContactListScreenInjector {
    fun getContactListComponentBuilder(): ContactListControllerComponent.Builder
}