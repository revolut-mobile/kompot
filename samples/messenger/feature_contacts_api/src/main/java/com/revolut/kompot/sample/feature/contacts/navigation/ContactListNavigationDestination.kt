package com.revolut.kompot.sample.feature.contacts.navigation

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.InternalDestination
import kotlinx.parcelize.Parcelize

@Parcelize
object ContactListNavigationDestination : InternalDestination<IOData.EmptyInput>(IOData.EmptyInput)