package com.revolut.kompot.sample.ui.flows.main

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.utils.viewBinding
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.databinding.FlowMainBinding
import com.revolut.kompot.sample.sampleApplication
import com.revolut.kompot.sample.ui.flows.main.MainFlowContract.Step
import com.revolut.kompot.sample.ui.flows.main.di.MainFlowComponent

class MainFlow : BaseFlow<Step, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

    override val layoutId = R.layout.flow_main
    private val binding by viewBinding(FlowMainBinding::bind)
    override var cacheStrategy: ControllerCacheStrategy = ControllerCacheStrategy.Prioritized
    override var keyInitialization = { MainFlowContract.mainFlowKey }

    override val component: MainFlowComponent by lazy(LazyThreadSafetyMode.NONE) {
        activity.sampleApplication
            .appComponent
            .mainFlowComponent
            .inputData(inputData)
            .flow(this)
            .build()
    }

    override val flowModel by lazy(LazyThreadSafetyMode.NONE) {
        component.flowModel
    }

    override fun onAttach() {
        super.onAttach()

        flowModel.tabsStateFlow()
            .collectTillDetachView { tabsState ->
                binding.bottomBar.setItems(tabsState.tabs)
                binding.bottomBar.setSelected(tabsState.selectedTabId)
            }
        binding.bottomBar.selectedItemFlow()
            .collectTillDetachView { tabId ->
                flowModel.onTabSelected(tabId)
            }
    }

    override fun updateUi(step: Step) = Unit

}