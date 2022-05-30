package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.binder.asFlow
import com.revolut.kompot.navigable.screen.ScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.resultStream() = resultsBinder().asFlow()

fun <UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> ScreenModel<UI_STATE, OUTPUT>.backStream() = backPressBinder().asFlow()