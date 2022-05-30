package com.revolut.kompot.navigable.flow

import android.os.Bundle
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.ModelBinder

interface FlowModel<STEP : FlowStep, OUTPUT : IOData.Output> {
    val step: STEP

    val hasBackStack: Boolean

    val hasChildFlow: Boolean

    val animation: TransitionAnimation

    val restorationNeeded: Boolean

    fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT>>

    fun getController(): Controller

    fun restorePreviousState()

    fun setNextState(
        step: STEP,
        animation: TransitionAnimation,
        addCurrentStepToBackStack: Boolean,
        childFlowModel: FlowModel<*, *>?
    )

    fun updateChildFlowState(childFlowModel: FlowModel<*, *>?)

    fun updateCurrentScreenState(state: Bundle)

    fun saveState(outState: Bundle)

    fun restoreState(restorationPolicy: RestorationPolicy)

    fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean

}