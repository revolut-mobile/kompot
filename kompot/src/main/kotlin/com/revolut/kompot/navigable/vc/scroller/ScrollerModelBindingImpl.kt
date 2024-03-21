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

package com.revolut.kompot.navigable.vc.scroller

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.coroutines.Direct
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.flow.ensureAvailability
import com.revolut.kompot.navigable.flow.quitFlow
import com.revolut.kompot.navigable.flow.scroller.ScrollerFlowControllersAdapter
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.parent.ParentControllerModelBindingDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
internal class ScrollerModelBindingImpl<M : ScrollerViewModel<S, Out>, S : ScrollerItem, Out : IOData.Output>(
    private val controller: ScrollerViewController<S>,
    private val itemContainerLayoutId: Int,
    private val scrollMode: ScrollMode,
    val model: M,
    private val recyclerViewId: Int,
    private val controllersAdapter: ScrollerFlowControllersAdapter<S> = ScrollerFlowControllersAdapter(
        layoutContainerId = itemContainerLayoutId,
        parentController = controller as Controller,
        controllersCache = controller.controllersCache,
        controllersFactory = model.scrollerCoordinator::getController,
    ),
    private val parentControllerModelBindingDelegate: ParentControllerModelBindingDelegate = ParentControllerModelBindingDelegate(
        childControllerManagersProvider = controllersAdapter,
        controller = controller,
    )
) : ScrollerModelBinding<S>, ModelBinding by parentControllerModelBindingDelegate {

    private val viewController: ViewController<*> get() = controller as ViewController<*>

    private val navActionsScheduler: NavActionsScheduler
        get() = viewController.findRootFlow().navActionsScheduler

    private val layoutManager by lazy(LazyThreadSafetyMode.NONE) {
        val orientation = when (scrollMode) {
            ScrollMode.VERTICAL -> RecyclerView.VERTICAL
            ScrollMode.HORIZONTAL,
            ScrollMode.PAGER -> RecyclerView.HORIZONTAL
        }
        LinearLayoutManagerImpl(viewController.activity, orientation, false, 1)
    }

    private var _recyclerView: RecyclerView? = null
    private val recyclerView get() = checkNotNull(_recyclerView)

    override fun onCreate() {
        setupScrollerRecycler()
        bindCoordinator()
    }

    override fun onDestroy() {
        parentControllerModelBindingDelegate.onDestroy()
        controllersAdapter.updateCache(controllersAdapter.currentList, emptyList())
    }

    private fun bindCoordinator() {
        model.scrollerCoordinator.scrollerCommandsStream()
            .onEach { command ->
                processScrollerCommand(command)
            }
            .flowOn(Dispatchers.Direct)
            .launchIn(viewController.createdScope)
        model.scrollerCoordinator.itemUpdatesStream()
            .onEach { itemsUpdate -> submitItemsUpdate(itemsUpdate) }
            .launchIn(viewController.createdScope)
    }

    private fun setupScrollerRecycler() {
        _recyclerView = viewController.view.findViewById(recyclerViewId)
            ?: throw IllegalStateException("${this::class.java.simpleName}: recyclerViewId is not valid. Forgot to override?")
        recyclerView.apply {
            layoutManager = this@ScrollerModelBindingImpl.layoutManager
            adapter = controllersAdapter
            if (scrollMode == ScrollMode.PAGER) {
                setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
                PagerSnapHelper().attachToRecyclerView(this)
            }

            addOnScrollListener(ScrollStateListener())
        }
    }

    private fun submitItemsUpdate(itemsUpdate: ScrollerItemsUpdate<S>) {
        if (controllersAdapter.currentList != itemsUpdate.items) {
            controllersAdapter.updateCache(controllersAdapter.currentList, itemsUpdate.items)
            controllersAdapter.submitList(itemsUpdate.items) {
                scrollToSelectedItem(itemsUpdate)
            }
        } else {
            scrollToSelectedItem(itemsUpdate)
        }
    }

    private fun scrollToSelectedItem(itemsUpdate: ScrollerItemsUpdate<S>) {
        val itemId = itemsUpdate.selectedItemId ?: return

        val position = controllersAdapter.currentList.indexOfFirst { it.id == itemId }
        if (position in 0..layoutManager.itemCount) {
            if (itemsUpdate.smoothScroll) {
                //We need to queue the smooth scrolls until everything is laid out
                //other wise can end up in weird states
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(position)
                }
            } else {
                //because we are queuing smooth ones, we have to queue normal scrolls as well
                //so we don't have jumps in-between
                recyclerView.post {
                    layoutManager.scrollToPosition(position)
                }
            }
        }
    }

    private fun processScrollerCommand(command: ScrollerCommand) {
        when (command) {
            is ScrollerCommand.Quit -> {
                if (!navActionsScheduler.ensureAvailability(command)) return
                Preconditions.requireMainThread("ScrollerCoordinator#quit()")
                viewController.quitFlow(navActionsScheduler)
            }
        }
    }

    private inner class ScrollStateListener : RecyclerView.OnScrollListener() {
        private var latestCompletelyVisiblePosition = -1

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val position = this@ScrollerModelBindingImpl.layoutManager.findFirstCompletelyVisibleItemPosition()
                if (latestCompletelyVisiblePosition != position && position in (0..controllersAdapter.currentList.size)) {
                    latestCompletelyVisiblePosition = position
                    controller.onCompletelyVisibleItemChanged(controllersAdapter.currentList[position])
                }
            }
        }
    }

    private class LinearLayoutManagerImpl(
        context: Context,
        @RecyclerView.Orientation orientation: Int,
        reverseLayout: Boolean,
        private val preloadItems: Int,
    ): LinearLayoutManager(context, orientation, reverseLayout) {

        override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
            val pageSize = getPageSize()
            val offscreenSpace = pageSize * preloadItems
            extraLayoutSpace[0] = offscreenSpace
            extraLayoutSpace[1] = offscreenSpace
        }

        private fun getPageSize(): Int {
            return if (orientation == RecyclerView.HORIZONTAL) width - paddingLeft - paddingRight else height - paddingTop - paddingBottom
        }

    }
}

@Suppress("FunctionName")
fun <M : ScrollerViewModel<S, Out>, S : ScrollerItem, Out : IOData.Output> ScrollerViewController<S>.ModelBinding(
    itemContainerLayoutId: Int = R.layout.flow_scroller_item_container,
    scrollMode: ScrollMode = ScrollMode.PAGER,
    recyclerViewId: Int = R.id.recyclerView,
    model: M,
): ScrollerModelBinding<S> {
    return ScrollerModelBindingImpl(
        controller = this,
        itemContainerLayoutId = itemContainerLayoutId,
        scrollMode = scrollMode,
        model = model,
        recyclerViewId = recyclerViewId,
    )
}