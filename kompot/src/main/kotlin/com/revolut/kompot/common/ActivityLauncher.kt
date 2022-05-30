package com.revolut.kompot.common

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.RootControllerManager

internal interface ActivityLauncher {
    fun startActivity(intent: Intent)
    fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle? = null)
}

internal class ActivityFromFragmentLauncher(private val fragment: Fragment) : ActivityLauncher {
    override fun startActivity(intent: Intent) = fragment.startActivity(intent)
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) =
        fragment.startActivityForResult(intent, requestCode, options)
}

internal class ActivityFromControllerLauncher(private val controller: Controller) : ActivityLauncher {

    override fun startActivity(intent: Intent) {
        getRootControllerManager().startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        getRootControllerManager().startActivityForResult(intent, requestCode, options)
    }

    private fun getRootControllerManager() = controller.getTopFlow().parentControllerManager as? RootControllerManager
        ?: throw IllegalStateException("RootControllerManager must be present for a controller")
}