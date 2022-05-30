package com.revolut.kompot.navigable.flow.scroller

import com.revolut.kompot.ExperimentalKompotApi
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.ParentFlow

@ExperimentalKompotApi
interface ScrollerFlow<OUTPUT_DATA : IOData.Output> : ParentFlow {
    var onFlowResult: (data: OUTPUT_DATA) -> Unit
}