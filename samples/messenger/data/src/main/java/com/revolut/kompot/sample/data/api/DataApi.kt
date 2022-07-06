package com.revolut.kompot.sample.data.api

import com.revolut.kompot.sample.data.database.ChatDao
import com.revolut.kompot.sample.data.utils.ContextProvider

interface DataApi {

    val contextProvider: ContextProvider

    val chatDao: ChatDao

}