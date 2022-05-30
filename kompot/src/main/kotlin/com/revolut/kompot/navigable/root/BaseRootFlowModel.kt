package com.revolut.kompot.navigable.root

import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.common.NavigationEventHandledResult
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep

abstract class BaseRootFlowModel<STATE : FlowState, STEP : FlowStep> : BaseFlowModel<STATE, STEP, IOData.EmptyOutput>() {
    open val onExternalActivityOpened: () -> Unit = {}
    internal lateinit var rootNavigator: RootNavigator

    override fun onCreated() {
        super.onCreated()

        rootNavigator.addOpenExternalForResultListener(onExternalActivityOpened)
    }

    override fun onFinished() {
        super.onFinished()

        rootNavigator.removeOpenExternalForResultListener(onExternalActivityOpened)
    }

    override fun tryHandleEvent(event: Event): EventResult? {
        //First we handle navigation in successors of BaseRootFlowModel
        //and if it's not handled we handle the navigation here.
        //This way the logic in successors in handleNavigationDestination will be handled first
        //that keeps the behaviour consistent with handleNavigationDestination logic.
        val superResult = super.tryHandleEvent(event)
        if(superResult != null) return superResult

        if (event is NavigationEvent) {
            when (event.destination) {
                is ModalDestination -> rootNavigator.openModal(event.destination, event.controller)
                is ExternalDestination -> handleExternalDestination(event.destination, event.controller)
                else -> return super.tryHandleEvent(event)
            }

            return NavigationEventHandledResult
        }
        return null
    }

    private fun handleExternalDestination(destination: ExternalDestination, controller: Controller?) {
        when (destination) {
            is ExternalDestination.Browser -> rootNavigator.openWebPage(destination.url)
            else -> rootNavigator.openExternal(destination, controller)
        }
    }
}