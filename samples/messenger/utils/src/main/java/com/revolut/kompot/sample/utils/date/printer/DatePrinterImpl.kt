package com.revolut.kompot.sample.utils.date.printer

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

internal class DatePrinterImpl @Inject constructor() : DatePrinter {

    private companion object {
        const val TIME_FORMAT = "HH:mm"
    }

    private val timeSdf by lazy {
        SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    }

    override fun printTime(timestamp: Date): String = timeSdf.format(timestamp)

}