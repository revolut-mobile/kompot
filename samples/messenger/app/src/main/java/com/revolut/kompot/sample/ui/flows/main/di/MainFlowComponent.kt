package com.revolut.kompot.sample.ui.flows.main.di

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.di.scope.FlowScope
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract
import dagger.BindsInstance
import dagger.Subcomponent

@FlowScope
@Subcomponent(
    modules = [MainFlowModule::class]
)
interface MainFlowComponent : BaseFlowComponent {
    val flowModel: MainFlowContract.FlowModelApi

    @Subcomponent.Builder
    interface Builder : BaseFlowComponent.Builder<MainFlowComponent, Builder> {
        @BindsInstance
        fun inputData(inputData: IOData.EmptyInput): Builder
    }

}