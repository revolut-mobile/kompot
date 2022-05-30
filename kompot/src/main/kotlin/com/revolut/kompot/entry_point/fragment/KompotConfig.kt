package com.revolut.kompot.entry_point.fragment

import androidx.annotation.LayoutRes
import com.revolut.kompot.R
import com.revolut.kompot.entry_point.KompotDelegate
import com.revolut.kompot.navigable.root.RootFlow

data class KompotConfig(
    val rootFlow: RootFlow<*, *>,
    @LayoutRes val defaultFlowLayout: Int? = R.layout.base_flow_container,
    val trimCacheThreshold: Int = DEFAULT_TRIM_CACHE_THRESHOLD,
    val savedStateEnabled: Boolean = true,
    val fullScreenEnabled: Boolean = true,
) {

    companion object {
        const val DEFAULT_TRIM_CACHE_THRESHOLD = 30
    }
}

internal fun KompotConfig.createDelegate() = KompotDelegate(
    rootFlow = rootFlow,
    defaultFlowLayout = defaultFlowLayout,
    trimCacheThreshold = trimCacheThreshold,
    savedStateEnabled = savedStateEnabled,
    fullScreenEnabled = fullScreenEnabled,
)