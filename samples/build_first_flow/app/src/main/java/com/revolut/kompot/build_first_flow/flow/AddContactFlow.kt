package com.revolut.kompot.build_first_flow.flow

import com.revolut.kompot.build_first_flow.App
import com.revolut.kompot.build_first_flow.R
import com.revolut.kompot.build_first_flow.flow.di.AddContactFlowComponent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.view.ControllerContainerFrameLayout

class AddContactFlow : RootFlow<AddContactFlowContract.Step, IOData.EmptyInput>(IOData.EmptyInput) {

    override val layoutId = R.layout.flow_root
    override val fitStatusBar: Boolean = true

    override val component: AddContactFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        (activity.application as App)
            .appComponent
            .addContactFlowComponent
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }

    override val containerForModalNavigation: ControllerContainerFrameLayout
        get() = view.findViewById(R.id.containerModal)
}