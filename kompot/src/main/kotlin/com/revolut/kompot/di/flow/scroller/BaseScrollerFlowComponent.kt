package com.revolut.kompot.di.flow.scroller

import com.revolut.kompot.di.flow.ParentFlowComponent
import com.revolut.kompot.navigable.flow.scroller.BaseScrollerFlow
import dagger.BindsInstance

interface BaseScrollerFlowComponent : ParentFlowComponent {
    interface Builder<T : BaseScrollerFlowComponent, B> {

        @BindsInstance
        fun flow(flow: BaseScrollerFlow<*, *, *>): B

        fun build(): T

    }
}