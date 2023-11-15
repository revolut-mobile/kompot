/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.navigable.cache

import com.revolut.kompot.BuildConfig
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.BaseFlowModel
import timber.log.Timber

class DefaultControllersCache(
    private val trimCacheThreshold: Int
) : ControllersCache {

    private val weakCacheSize: Int
        get() = weakScreenControllersMap.size + weakFlowControllersMap.size

    private val strongControllersMap = mutableMapOf<ControllerKey, Controller>()

    private val weakScreenControllersMap = LinkedHashMap<ControllerKey, Controller>()
    private val weakFlowControllersMap = mutableMapOf<ControllerKey, BaseFlow<*, *, *>>()

    private val dependsMap = mutableMapOf<ControllerKey, List<ControllerKey>>()
    private val dependantsGlobalSet = mutableSetOf<ControllerKey>()

    override fun onControllerCreated(controller: Controller) {
        when (val strategy = controller.cacheStrategy) {
            is ControllerCacheStrategy.Ignored -> return
            is ControllerCacheStrategy.Auto -> {
                addWeakController(controller)
            }
            is ControllerCacheStrategy.DependentOn -> {
                addDependentController(strategy, controller)
            }
            is ControllerCacheStrategy.Prioritized -> {
                addStrongController(controller)
            }
        }
        printCacheSize()
    }

    private fun addWeakController(controller: Controller) {
        if (controller is BaseFlow<*, *, *>) {
            weakFlowControllersMap[controller.key] = controller
            //return immediately and skip cache trimming because it will be
            //launched with first child of this flow added to controller
            return
        }
        weakScreenControllersMap[controller.key] = controller
        if (weakCacheSize > trimCacheThreshold && weakScreenControllersMap.isNotEmpty()) {
            val eldestController = weakScreenControllersMap.entries.first()
            if (eldestController.key != controller.key) {
                removeControllerInternal(eldestController.key, destroy = true, trimParent = true)
            }
        }
    }

    private fun addDependentController(
        strategy: ControllerCacheStrategy.DependentOn,
        controller: Controller
    ) {
        strongControllersMap[controller.key] = controller

        val holderKey = strategy.key
        val dependants = dependsMap[holderKey] ?: emptyList()
        dependsMap[holderKey] = dependants + controller.key
        dependantsGlobalSet.add(controller.key)
    }

    private fun addStrongController(controller: Controller) {
        strongControllersMap[controller.key] = controller
    }

    override fun onControllerDestroyed(controller: Controller) {
        removeByKey(controller.key, finish = false, trimParent = false)
    }

    override fun removeController(controllerKey: ControllerKey, finish: Boolean, doAfterRemove: () -> Unit) {
        removeControllerInternal(controllerKey, finish, trimParent = false, doAfterRemove)
    }

    private fun removeControllerInternal(
        controllerKey: ControllerKey,
        destroy: Boolean,
        trimParent: Boolean,
        doAfterRemove: () -> Unit = {}
    ) {
        if (!dependantsGlobalSet.contains(controllerKey)) {
            removeByKey(controllerKey, destroy, trimParent)
            doAfterRemove()
        }
    }

    private fun removeByKey(controllerKey: ControllerKey, finish: Boolean, trimParent: Boolean) {
        val targetController = getController(controllerKey)?.apply {
            if (!destroyed && finish) {
                finish()
            }
        }
        strongControllersMap.remove(controllerKey)
        weakScreenControllersMap.remove(controllerKey)
        dependsMap[controllerKey]?.forEach { key ->
            removeByKey(key, true, trimParent)
        }
        dependsMap.remove(controllerKey)
        dependantsGlobalSet.remove(controllerKey)
        weakFlowControllersMap.remove(controllerKey)

        if (trimParent && targetController != null) {
            tryRemoveParent(targetController)
        }

        printCacheSize()
    }

    private fun tryRemoveParent(childController: Controller) {
        val parentKey = childController.parentController?.key ?: return
        val parentController = weakFlowControllersMap[parentKey]
        if (parentController?.canBeRemoved() == true) {
            removeByKey(parentKey, finish = true, trimParent = true)
        }
    }

    private fun BaseFlow<*, *, *>.canBeRemoved(): Boolean {
        val flowModel = getFlowModel() as? BaseFlowModel<*, *, *>
        val currentStateWrapper = flowModel?.stateWrapper ?: return false
        val children = listOf(currentStateWrapper) + flowModel.backStack
        children.forEach { stateWrapper ->
            val controller = stateWrapper.currentControllerKey?.let { key ->
                getController(key)
            }
            if (controller == null || (controller is BaseFlow<*, *, *> && controller.canBeRemoved())) {
                //keep search for children that block flow from being removed
                return@forEach
            }
            //flow can't be removed because one if its children is still in cache
            return false
        }
        return true
    }

    override fun getController(controllerKey: ControllerKey): Controller? =
        weakScreenControllersMap[controllerKey] ?: weakFlowControllersMap[controllerKey] ?: strongControllersMap[controllerKey]

    override fun isControllerCached(controllerKey: ControllerKey): Boolean = getController(controllerKey) != null

    override fun clearCache() {
        strongControllersMap.clear()
        weakScreenControllersMap.clear()
        dependsMap.clear()
        dependantsGlobalSet.clear()
        weakFlowControllersMap.clear()
        printCacheSize()
    }

    private fun printCacheSize() {
        if (!BuildConfig.DEBUG) {
            return
        }

        val strongControllersCount = strongControllersMap.size
        val weakScreensCount = weakScreenControllersMap.size
        val weakFlowsCount = weakFlowControllersMap.size
        val totalSize = strongControllersCount + weakScreensCount + weakFlowsCount

        Timber.d("--------- Cache statistics ---------")
        Timber.d("Size $totalSize (strong: $strongControllersCount; weak: $weakCacheSize)")
        if (strongControllersMap.values.isNotEmpty()) {
            Timber.d("Strong storing: ")
            strongControllersMap.values.reversed().forEach { controller ->
                Timber.d("    ${controller.controllerName} (${controller.fullControllerName})")
            }
        }

        if (weakScreenControllersMap.values.isNotEmpty()) {
            Timber.d("Weak storing: ")
            weakScreenControllersMap.values.reversed().forEach { controller ->
                Timber.d("    ${controller.controllerName} (${controller.fullControllerName})")
            }
        }

        if (weakFlowControllersMap.values.isNotEmpty()) {
            Timber.d("Flow Weak storing: ")
            weakFlowControllersMap.values.reversed().forEach { controller ->
                Timber.d("    ${controller.controllerName} (${controller.fullControllerName})")
            }
        }

        dependsMap.keys.mapNotNull { key -> getController(key) }.forEach { primaryController ->
            Timber.d("${primaryController.controllerName} dependants:")
            dependsMap[primaryController.key]?.forEach { dependant ->
                getController(dependant)?.run {
                    Timber.d("    $controllerName")
                }
            }
        }
    }

    override fun getCacheLogWithKeys(): String {

        return buildString {

            val strongControllersCount = strongControllersMap.size
            val weakScreensCount = weakScreenControllersMap.size
            val weakFlowsCount = weakFlowControllersMap.size
            val totalSize = strongControllersCount + weakScreensCount + weakFlowsCount

            appendLine("--------- Cache statistics ---------")
            appendLine("Size $totalSize (strong: $strongControllersCount; weak: $weakCacheSize)")
            if (strongControllersMap.values.isNotEmpty()) {
                appendLine("Strong storing: ")
                strongControllersMap.entries.reversed().forEach { (key, controller) ->
                    appendLine("    ${controller.controllerName} (${controller.fullControllerName})  key: ${key.value}")
                }
            }

            if (weakScreenControllersMap.values.isNotEmpty()) {
                appendLine("Weak storing: ")
                weakScreenControllersMap.entries.reversed().forEach { (key, controller) ->
                    appendLine("    ${controller.controllerName} (${controller.fullControllerName})  key: ${key.value}")
                }
            }

            if (weakFlowControllersMap.values.isNotEmpty()) {
                appendLine("Flow Weak storing: ")
                weakFlowControllersMap.entries.reversed().forEach { (key, controller) ->
                    appendLine("    ${controller.controllerName} (${controller.fullControllerName})  key: ${key.value})")
                }
            }

            dependsMap.keys.forEach { key ->
                appendLine("$key dependants:")
                dependsMap[key]?.forEach { dependant ->
                    appendLine("    $dependant")
                }
            }
        }
    }
}