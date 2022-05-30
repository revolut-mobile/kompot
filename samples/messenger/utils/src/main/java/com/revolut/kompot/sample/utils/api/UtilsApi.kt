package com.revolut.kompot.sample.utils.api

import com.revolut.kompot.sample.utils.date.printer.DatePrinter
import com.revolut.kompot.sample.utils.date.provider.DateProvider

interface UtilsApi {
    val datePrinter: DatePrinter
    val dateProvider: DateProvider
}