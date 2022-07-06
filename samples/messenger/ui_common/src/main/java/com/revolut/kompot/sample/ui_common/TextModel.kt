package com.revolut.kompot.sample.ui_common

import androidx.annotation.ColorRes

data class TextModel(
    val content: String,
    @ColorRes val color: Int? = null
)