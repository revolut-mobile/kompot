package com.revolut.kompot.common

import androidx.fragment.app.Fragment
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.RootControllerManager

internal interface PermissionsRequester {
    fun requestPermissions(permissions: List<String>, requestCode: Int)
}

internal class PermissionsFromControllerRequester(private val controller: Controller): PermissionsRequester {

    override fun requestPermissions(permissions: List<String>, requestCode: Int) {
        val rootControllerManager = controller.getTopFlow().parentControllerManager as? RootControllerManager
            ?: throw IllegalStateException("RootControllerManager must be present for a controller")
        rootControllerManager.requestPermissions(permissions, requestCode)
    }
}

internal class FragmentPermissionsRequester(private val fragment: Fragment) : PermissionsRequester {
    override fun requestPermissions(permissions: List<String>, requestCode: Int) {
        fragment.requestPermissions(permissions.toTypedArray(), requestCode)
    }
}