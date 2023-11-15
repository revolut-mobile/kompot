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

package com.revolut.kompot.navigable.vc.ui.list

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.revolut.decorations.dividers.DelegatesDividerItemDecoration
import com.revolut.decorations.overlay.DelegatesOverlayItemDecoration
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.screen.BaseRecyclerViewLayoutManager
import com.revolut.kompot.navigable.vc.ViewController
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.ui.DebounceStreamProvider
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStateModelBindingImpl
import com.revolut.kompot.navigable.vc.ui.UIStatesModelBinding
import com.revolut.recyclerkit.delegates.RecyclerViewDelegate
import com.revolut.rxdiffadapter.RxDiffAdapter

internal typealias LayoutManagerProvider = (Context) -> LayoutManager

interface UIListStatesModelBinding<UI : States.UIList> : UIStatesModelBinding<UI> {
    val recyclerView: RecyclerView
    val layoutManager: LayoutManager
}

internal class UIListStatesModelBindingImpl<M : UIListStatesModel<D, UI, Out>, D : States.Domain, UI : States.UIList, Out : IOData.Output>(
    private val controller: UIListStatesController<UI>,
    private val model: M,
    private val debounceStreamProvider: DebounceStreamProvider?,
    private val recyclerViewId: Int,
    private val layoutManagerProvider: LayoutManagerProvider,
    private val delegates: List<RecyclerViewDelegate<*, *>>,
    private val listAdapter: RxDiffAdapter,
    private val uiStatesModelBinding: UIStateModelBindingImpl<M, D, UI, Out> = UIStateModelBindingImpl(
        controller,
        model,
        debounceStreamProvider
    )
) : UIListStatesModelBinding<UI>, UIStatesModelBinding<UI> by uiStatesModelBinding {

    private val viewController: ViewController<*> get() = controller as ViewController<*>
    private var _recyclerView: RecyclerView? = null

    override val layoutManager: LayoutManager by lazy(LazyThreadSafetyMode.NONE) {
        layoutManagerProvider(viewController.activity)
    }
    override val recyclerView: RecyclerView by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(_recyclerView)
    }

    override fun onCreate() {
        uiStatesModelBinding.onCreate()

        _recyclerView = viewController.view.findViewById(recyclerViewId)
            ?: throw IllegalStateException("${this::class.java.simpleName}: recyclerViewId is not valid. Forgot to override?")

        recyclerView.apply {
            adapter = listAdapter.also { adapter ->
                adapter.delegatesManager.addDelegates(delegates)
            }
            layoutManager = this@UIListStatesModelBindingImpl.layoutManager
            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = false
            }
            addItemDecoration(DelegatesDividerItemDecoration())
            addItemDecoration(DelegatesOverlayItemDecoration())
        }

        uiStatesModelBinding.doBeforeRender = { listAdapter.setItems(it.items) }
    }
}

@Suppress("FunctionName")
fun <M : UIListStatesModel<D, UI, Out>, D : States.Domain, UI : States.UIList, Out : IOData.Output> UIListStatesController<UI>.ModelBinding(
    model: M,
    delegates: List<RecyclerViewDelegate<*, *>>,
    recyclerViewId: Int = R.id.recyclerView,
    layoutManagerProvider: LayoutManagerProvider = { DefaultLayoutManager(this) },
    listAdapter: RxDiffAdapter = DefaultAdapter(),
    debounceStreamProvider: DebounceStreamProvider? = null,
): UIListStatesModelBinding<UI> {
    return UIListStatesModelBindingImpl(
        controller = this,
        model = model,
        debounceStreamProvider = debounceStreamProvider,
        recyclerViewId = recyclerViewId,
        layoutManagerProvider = layoutManagerProvider,
        delegates = delegates,
        listAdapter = listAdapter
    )
}

@Suppress("FunctionName")
internal fun DefaultLayoutManager(controller: ViewControllerApi): LayoutManager =
    BaseRecyclerViewLayoutManager((controller as ViewController<*>).activity).apply {
        enablePredictiveItemAnimations = true
    }

@Suppress("FunctionName")
internal fun DefaultAdapter(): RxDiffAdapter =
    RxDiffAdapter(
        delegates = emptyList(),
        async = false,
        autoScrollToTop = false,
        detectMoves = true
    )