package com.revolut.kompot.navigable.flow

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.ParentFlow

interface Flow<OUTPUT_DATA : IOData.Output>: ParentFlow {

    var onFlowResult: (data: OUTPUT_DATA) -> Unit

}