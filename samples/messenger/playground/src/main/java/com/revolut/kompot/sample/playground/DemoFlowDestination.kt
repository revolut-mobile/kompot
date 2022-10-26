package com.revolut.kompot.sample.playground

import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.InternalDestination
import kotlinx.parcelize.Parcelize

@Parcelize
object DemoFlowDestination : InternalDestination<IOData.EmptyInput>(IOData.EmptyInput)