package com.revolut.kompot.sample.ui.flows.main

import android.view.View
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.composite.ui_states_flow.ModelBinding
import com.revolut.kompot.navigable.vc.composite.ui_states_flow.UIStatesFlowController
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.databinding.FlowMainBinding
import com.revolut.kompot.sample.sampleApplication
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.UIState
import com.revolut.kompot.sample.ui.flows.main.di.MainFlowComponent

class MainFlow : ViewController<IOData.EmptyOutput>(), UIStatesFlowController<UIState> {

    override val layoutId = R.layout.flow_main
    private val binding by viewBinding(FlowMainBinding::bind)
    override var cacheStrategy: ControllerCacheStrategy = ControllerCacheStrategy.Prioritized
    override var keyInitialization = { MainFlowContract.mainFlowKey }

    override val component: MainFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        activity.sampleApplication
            .appComponent
            .mainFlowComponent
            .controller(this)
            .build()
    }
    override val controllerModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }
    override val modelBinding by lazy(LazyThreadSafetyMode.NONE) {
        ModelBinding(
            model = controllerModel,
        )
    }

    override fun onShown(view: View) {
        binding.bottomBar.selectedItemFlow()
            .collectTillDetachView { tabId ->
                controllerModel.onTabSelected(tabId)
            }
    }

    override fun render(uiState: UIState, payload: Any?) {
        binding.bottomBar.setItems(uiState.tabs)
        binding.bottomBar.setSelected(uiState.selectedTabId)
    }
}