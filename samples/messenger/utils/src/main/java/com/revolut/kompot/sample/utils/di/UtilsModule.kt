package com.revolut.kompot.sample.utils.di

import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import com.revolut.kompot.sample.utils.date.printer.DatePrinterImpl
import com.revolut.kompot.sample.utils.date.provider.DateProvider
import com.revolut.kompot.sample.utils.date.provider.DateProviderImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
internal abstract class UtilsModule {

    @Binds
    @Singleton
    abstract fun bindDatePrinter(datePrinterImpl: DatePrinterImpl): DatePrinter

    @Binds
    @Singleton
    abstract fun bindDateProvider(dateProviderImpl: DateProviderImpl): DateProvider

}