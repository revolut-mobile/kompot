package com.revolut.kompot.navigable

import com.revolut.kompot.navigable.root.RootFlow

internal fun Controller.findRootFlow(): RootFlow<*, *> {
    if (this is RootFlow<*, *>) {
        return this
    }

    var parent = parentController
    while (parent != null && parent !is RootFlow<*, *>) {
        parent = parent.parentController
    }

    return (parent as? RootFlow<*, *>) ?: throw IllegalStateException("There is no RootFlow in the tree")
}