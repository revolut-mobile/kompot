package com.revolut.kompot.sample.utils.date.provider

import java.util.*

class MockDateProvider : DateProvider {

    private val date = Date(1000L)

    override fun provideDate(): Date = date

}