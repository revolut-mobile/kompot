package com.revolut.kompot.di.flow

import com.revolut.kompot.navigable.flow.BaseFlow
import dagger.BindsInstance

interface BaseFlowComponent : ParentFlowComponent {
    interface Builder<T : BaseFlowComponent, B> {

        @BindsInstance
        fun flow(flow: BaseFlow<*, *, *>): B

        fun build(): T

    }
}