package com.revolut.kompot.navigable.flow

import android.content.Intent
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.ControllerModel

internal class FlowLifecycleDelegate(
    private val controller: Controller,
    private val controllerModel: ControllerModel,
    private val childControllerManagers: () -> List<ControllerManager>,
    private val onActivityResultInternal: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
) {

    fun onCreate() {
        controllerModel.onLifecycleEvent(LifecycleEvent.CREATED)
    }

    fun onDestroy() {
        controllerModel.onLifecycleEvent(LifecycleEvent.FINISHED)
    }

    fun onAttach() {
        controllerModel.onLifecycleEvent(LifecycleEvent.SHOWN)
    }

    fun onDetach() {
        controllerModel.onLifecycleEvent(LifecycleEvent.HIDDEN)
        childControllerManagers().reversed().forEach { manager -> manager.onDetach() }
    }

    fun onTransitionStart(enter: Boolean) {
        childControllerManagers().forEach { manager ->
            manager.activeController?.onTransitionStart(enter)
        }
    }

    fun onTransitionEnd(enter: Boolean) {
        childControllerManagers().forEach { manager ->
            manager.activeController?.onTransitionEnd(enter)
        }
    }

    fun onHostPaused() {
        childControllerManagers().forEach { manager -> manager.onHostPaused() }
    }

    fun onHostResumed() {
        childControllerManagers().forEach { manager -> manager.onHostResumed() }
    }

    fun onHostStarted() {
        controller.onAttach()
        childControllerManagers().forEach { manager -> manager.onHostStarted() }
    }

    fun onHostStopped() {
        controller.onDetach()
        childControllerManagers().forEach { manager -> manager.onHostStopped() }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        controller.doOnAttach {
            onActivityResultInternal(requestCode, resultCode, data)
            childControllerManagers().forEach { manager -> manager.onActivityResult(requestCode, resultCode, data) }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        childControllerManagers().forEach { manager -> manager.onRequestPermissionsResult(requestCode, permissions, grantResults) }
    }
}