package com.revolut.kompot.build_first_screen.flow

import com.revolut.kompot.build_first_screen.App
import com.revolut.kompot.build_first_screen.R
import com.revolut.kompot.build_first_screen.flow.di.RootFlowComponent
import com.revolut.kompot.common.IOData
import com.revolut.kompot.dialog.DefaultLoadingDialogDisplayer
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.view.ControllerContainerFrameLayout

class RootFlowImpl : RootFlow<RootFlowContract.Step, IOData.EmptyInput>(IOData.EmptyInput) {

    override val layoutId = R.layout.flow_root

    override val component: RootFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        (activity.application as App)
            .appComponent
            .rootFlowComponent
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }

    override val containerForModalNavigation: ControllerContainerFrameLayout
        get() = view.findViewById(R.id.containerModal)
}