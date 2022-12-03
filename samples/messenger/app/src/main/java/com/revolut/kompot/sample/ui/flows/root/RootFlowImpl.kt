package com.revolut.kompot.sample.ui.flows.root

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.sample.Features
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.databinding.FlowRootBinding
import com.revolut.kompot.sample.sampleApplication
import com.revolut.kompot.sample.ui.flows.root.di.RootFlowComponent
import com.revolut.kompot.view.ControllerContainerFrameLayout

class RootFlowImpl : RootFlow<RootFlowContract.Step, IOData.EmptyInput>(IOData.EmptyInput) {

    override val layoutId = R.layout.flow_root
    private val binding by viewBinding(FlowRootBinding::bind)

    override val component: RootFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        activity.sampleApplication
            .appComponent
            .rootFlowComponent
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }

    override val containerForModalNavigation: ControllerContainerFrameLayout
        get() = binding.containerModal

    override fun onCreateFlowView(view: View) {
        super.onCreateFlowView(view)

        component.featureRegistry.registerFeatures(Features.createFeatures())
    }
}