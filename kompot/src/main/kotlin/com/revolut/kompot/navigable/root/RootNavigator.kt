package com.revolut.kompot.navigable.root

import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.toIntent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.view.ControllerContainer

internal class RootNavigator(private val rootFlow: RootFlow<*, *>) {
    private val addOpenExternalForResultListeners = mutableListOf<() -> Unit>()

    fun openModal(destination: ModalDestination, callerController: Controller) {
        if (destination is ModalDestination.ExplicitScreen<*>) {
            openModalScreen(destination, callerController)
        } else if (destination is ModalDestination.ExplicitFlow<*>) {
            openModalFlow(destination, callerController)
        }
    }

    private fun <T : IOData.Output> openModalScreen(destination: ModalDestination.ExplicitScreen<T>, callerController: Controller) {
        rootFlow.open(
            controller = destination.screen.apply {
                (this as Controller).run {
                    doOnCreate {
                        (view as ControllerContainer).fitStatusBar = true
                    }
                }
                onScreenResult = { result ->
                    (this as Controller).parentControllerManager.clear()
                    destination.onResult?.invoke(result)
                }
            } as Controller,
            style = destination.style,
            parentController = callerController.getFlow()
        )
    }

    private fun <T : IOData.Output> openModalFlow(destination: ModalDestination.ExplicitFlow<T>, callerController: Controller) {
        rootFlow.open(
            controller = destination.flow.apply {
                (this as Controller).run {
                    doOnCreate {
                        (view as ControllerContainer).fitStatusBar = true
                    }
                }
                onFlowResult = { result ->
                    (this as Controller).parentControllerManager.clear()
                    destination.onResult?.invoke(result)
                }
            } as Controller,
            style = destination.style,
            parentController = callerController.getFlow()
        )
    }

    private fun Controller.getFlow(): Controller? = if (this is BaseFlow<*, *, *>) {
        this
    } else {
        parentController
    }

    fun openExternal(destination: ExternalDestination, controller: Controller?) {
        if (destination.requestCode == null) {
            (controller ?: rootFlow).startActivity(destination.toIntent(rootFlow.activity))
        } else {
            addOpenExternalForResultListeners.forEach { listener -> listener() }
            (controller ?: rootFlow).startActivityForResult(destination.toIntent(rootFlow.activity), destination.requestCode ?: 0)
        }
    }

    fun addOpenExternalForResultListener(listener: () -> Unit) {
        addOpenExternalForResultListeners.add(listener)
    }

    fun removeOpenExternalForResultListener(listener: () -> Unit) {
        addOpenExternalForResultListeners.remove(listener)
    }

    fun openWebPage(url: String) {
        openExternal(
            ExternalDestination.Browser(url),
            rootFlow
        )
    }

}
