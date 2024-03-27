package com.revolut.kompot.sample.ui.flows.root

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.dialog.DefaultLoadingDialogDisplayer
import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.view.ControllerContainerFrameLayout
import com.revolut.kompot.sample.Features
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.databinding.FlowRootBinding
import com.revolut.kompot.sample.sampleApplication
import com.revolut.kompot.sample.ui.flows.root.di.RootFlowComponent
import com.revolut.kompot.navigable.utils.viewBinding

class RootFlowImpl : RootFlow<RootFlowContract.Step, IOData.EmptyInput>(IOData.EmptyInput) {

    override val rootDialogDisplayer by lazy(LazyThreadSafetyMode.NONE) {
        DialogDisplayer(
            loadingDialogDisplayer = DefaultLoadingDialogDisplayer(activity),
            delegates = emptyList()
        )
    }

    override val layoutId = R.layout.flow_root
    private val binding by viewBinding(FlowRootBinding::bind)

    override val controllerName = "Root"

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

        component.featuresRegistry.registerFeatures(Features.createFeaturesList())
    }
}