package com.revolut.kompot.navigable.screen

import com.revolut.kompot.common.IOData

interface Screen<OUTPUT_DATA : IOData.Output> {
    var onScreenResult: (data: OUTPUT_DATA) -> Unit
}