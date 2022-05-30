package com.revolut.kompot.navigable.flow

import android.os.Bundle

sealed class RestorationPolicy {

    internal open val postponed: Boolean = false

    data class FromBundle(val bundle: Bundle, override val postponed: Boolean = false) : RestorationPolicy()
    data class FromParent(val parentFlowModel: FlowModel<*, *>, override val postponed: Boolean = false) : RestorationPolicy()
}