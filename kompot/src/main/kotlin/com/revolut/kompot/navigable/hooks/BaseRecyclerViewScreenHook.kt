package com.revolut.kompot.navigable.hooks

import androidx.recyclerview.widget.RecyclerView

class BaseRecyclerViewScreenHook(val patchRecyclerView: RecyclerView.() -> Unit) : ControllerHook {
    companion object Key : ControllerHook.Key<BaseRecyclerViewScreenHook>
}