package com.revolut.kompot.sample.ui.flows.root

import com.revolut.kompot.FeaturesManager
import com.revolut.kompot.common.InternalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.root.BaseRootFlowModel
import com.revolut.kompot.sample.ui.flows.main.MainFlow
import javax.inject.Inject

class RootFlowModel @Inject constructor(
    private val featuresManager: FeaturesManager
) : BaseRootFlowModel<RootFlowContract.State, RootFlowContract.Step>(),
    RootFlowContract.FlowModelApi {

    override val initialStep = RootFlowContract.Step.MainFlow
    override val initialState = RootFlowContract.State()

    override fun getController(step: RootFlowContract.Step): Controller = when (step) {
        is RootFlowContract.Step.MainFlow -> MainFlow()
        is RootFlowContract.Step.FeatureManagerStep -> featuresManager.getController(step.featureFlowStep, this)
    }

    override fun handleErrorEvent(throwable: Throwable): Boolean {
        return true
    }

    override fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean = when (navigationDestination) {
        is InternalDestination<*> -> {
            featuresManager.handleDestination(navigationDestination)?.run {
                next(
                    RootFlowContract.Step.FeatureManagerStep(step),
                    addCurrentStepToBackStack = navigationDestination.addCurrentStepToBackStack,
                    animation = animation
                )
            } != null
        }
        else -> false
    }

}
