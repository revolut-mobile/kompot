package com.revolut.kompot.sample.utils.date.provider

import java.util.*
import javax.inject.Inject

internal class DateProviderImpl @Inject constructor() : DateProvider {
    override fun provideDate(): Date = Date()
}