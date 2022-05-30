package com.revolut.kompot.navigable

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.revolut.kompot.common.ActivityLauncher
import com.revolut.kompot.common.PermissionsRequester
import com.revolut.kompot.holder.RootControllerViewHolder
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.RestorationPolicy
import com.revolut.kompot.navigable.hooks.HooksProvider
import com.revolut.kompot.navigable.root.RootFlow

internal class RootControllerManager(
    private val rootFlow: RootFlow<*, *>,
    private val activityLauncher: ActivityLauncher,
    private val permissionsRequester: PermissionsRequester,
    @LayoutRes defaultFlowLayout: Int?,
    controllersCache: ControllersCache,
    hooksProvider: HooksProvider,
) : ControllerManager(
    modal = false,
    defaultFlowLayout = defaultFlowLayout,
    controllersCache = controllersCache,
    controllerViewHolder = RootControllerViewHolder(),
    onAttachController = null,
    onDetachController = null,
), ActivityLauncher by activityLauncher, PermissionsRequester by permissionsRequester {

    private val rootControllerViewHolder
        get() = controllerViewHolder as RootControllerViewHolder

    init {
        this.hooksProvider = hooksProvider
    }

    fun showRootFlow(savedState: Bundle?, hostContainer: ViewGroup) {
        rootControllerViewHolder.setContainer(hostContainer)

        savedState?.let { bundle ->
            rootFlow.doOnCreate {
                rootFlow.restoreState(RestorationPolicy.FromBundle(bundle))
            }
        }
        showImmediately(rootFlow)
    }

    fun attachToHostContainer(container: ViewGroup) {
        rootControllerViewHolder.setContainer(container)
        activeController?.view?.let(controllerViewHolder::add)
    }

    fun detachFromHostContainer() {
        activeController?.view?.let(controllerViewHolder::remove)
        rootControllerViewHolder.removeContainer()
    }

    internal fun saveState(outState: Bundle) {
        rootFlow.saveState(outState)
    }

    override fun onDestroy() {
        controllersCache.clearCache()
        super.onDestroy()
    }

}