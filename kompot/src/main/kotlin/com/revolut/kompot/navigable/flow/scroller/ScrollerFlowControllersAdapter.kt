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

package com.revolut.kompot.navigable.flow.scroller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.revolut.kompot.holder.DefaultControllerViewHolder
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.flow.ControllerManagersProvider
import com.revolut.kompot.navigable.vc.scroller.ScrollerItem
import java.util.LinkedList

internal class ScrollerFlowControllersAdapter<T : ScrollerItem>(
    @LayoutRes private val layoutContainerId: Int,
    private val parentController: Controller,
    private val controllersCache: ControllersCache,
    private val controllersFactory: (T) -> Controller,
) : ListAdapter<T, ScrollerFlowControllersAdapter.ControllerViewHolder>(ItemCallBack()), ControllerManagersProvider {

    private val controllerKeys = HashMap<T, ControllerKey>()
    val childControllerManagers = LinkedList<ControllerManager>()

    override val all: List<ControllerManager>
        get() = childControllerManagers

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerViewHolder {
        val container = LayoutInflater.from(parent.context).inflate(layoutContainerId, parent, false)
        return ControllerViewHolder(
            itemView = container as ViewGroup,
            controllersCache = controllersCache
        )
    }

    override fun onViewAttachedToWindow(holder: ControllerViewHolder) {
        super.onViewAttachedToWindow(holder)
        val controller = getController(currentList[holder.adapterPosition])
        holder.controllerManager.apply {
            onAttach()
            childControllerManagers.add(this)
            show(
                controller = controller,
                animation = TransitionAnimation.NONE,
                backward = false,
                parentController = parentController,
            )
        }
    }

    override fun onBindViewHolder(holder: ControllerViewHolder, position: Int) = Unit

    override fun onViewDetachedFromWindow(holder: ControllerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        removeController(holder)
    }

    override fun onViewRecycled(holder: ControllerViewHolder) {
        super.onViewRecycled(holder)
        removeController(holder)
    }

    fun updateCache(oldList: List<T>, newList: List<T>) {
        val itemsToRemove = oldList - newList.toSet()
        itemsToRemove.map(::removeControllerFromCacheByStep)
    }

    private fun removeControllerFromCacheByStep(item: T) {
        controllerKeys[item]?.let { controllerKey -> controllersCache.removeController(controllerKey, true) }
        controllerKeys.remove(item)
    }

    private fun removeController(holder: ControllerViewHolder) =
        holder.controllerManager.apply {
            detach()
            childControllerManagers.remove(this)
        }

    private fun ControllerManager.detach() {
        removeActiveController()
        onDetach()
    }

    private fun getController(item: T): Controller = controllerKeys[item]
        ?.let { controllersCache.getController(it) }
        ?: createControllerInternal(item)

    private fun createControllerInternal(item: T) =
        controllersFactory.invoke(item).also {
            it.cacheStrategy = ControllerCacheStrategy.Prioritized
            controllerKeys[item] = it.key
        }

    internal class ControllerViewHolder(
        itemView: ViewGroup,
        controllersCache: ControllersCache,
    ) : RecyclerView.ViewHolder(itemView) {
        val controllerManager = ControllerManager(
            controllerViewHolder = DefaultControllerViewHolder(itemView),
            modal = false,
            controllersCache = controllersCache,
            defaultControllerContainer = null,
            onTransitionCanceled = null,
        )
    }

    internal class ItemCallBack<T : ScrollerItem> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.equals(newItem)
        }
    }
}