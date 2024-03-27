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

package com.revolut.kompot.navigable.vc.composite.ui_states_scroller

import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.composite.CompositeModelBinding
import com.revolut.kompot.navigable.vc.scroller.ScrollMode
import com.revolut.kompot.navigable.vc.scroller.ScrollerItem
import com.revolut.kompot.navigable.vc.scroller.ScrollerModelBinding
import com.revolut.kompot.navigable.vc.scroller.ScrollerModelBindingImpl
import com.revolut.kompot.navigable.vc.scroller.ScrollerViewController
import com.revolut.kompot.navigable.vc.scroller.ScrollerViewModel
import com.revolut.kompot.navigable.vc.ui.DebounceStreamProvider
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStateModelBindingImpl
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import com.revolut.kompot.navigable.vc.ui.UIStatesModelBinding

interface UIStatesScrollerBinding<UI : States.UI, S : ScrollerItem> : ScrollerModelBinding<S>, UIStatesModelBinding<UI>

interface UIStatesScrollerController<UI : States.UI, S : ScrollerItem> : ScrollerViewController<S>,
    UIStatesController<UI> {
    override val modelBinding: UIStatesScrollerBinding<UI, S>
}

interface UIStatesScrollerModel<D : States.Domain, UI : States.UI, Item : ScrollerItem, Out : IOData.Output>
    : ScrollerViewModel<Item, Out>, UIStatesModel<D, UI, Out>

internal class UIStatesScrollerModelBindingImpl<D : States.Domain, UI : States.UI, S : ScrollerItem, Out : IOData.Output>(
    controller: UIStatesScrollerController<UI, S>,
    itemContainerLayoutId: Int,
    scrollMode: ScrollMode,
    model: UIStatesScrollerModel<D, UI, S, Out>,
    recyclerViewId: Int,
    debounceStreamProvider: DebounceStreamProvider?,
    private val scrollerModelBinding: ScrollerModelBinding<S> = ScrollerModelBindingImpl(
        controller = controller,
        itemContainerLayoutId = itemContainerLayoutId,
        scrollMode = scrollMode,
        model = model,
        recyclerViewId = recyclerViewId,
    ),
) : UIStatesScrollerBinding<UI, S>, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        scrollerModelBinding,
        UIStateModelBindingImpl(
            controller = controller,
            model = model,
            debounceStreamProvider = debounceStreamProvider,
        ),
    )
)

@Suppress("FunctionName")
fun <D : States.Domain, UI : States.UI, S : ScrollerItem, Out : IOData.Output> UIStatesScrollerController<UI, S>.ModelBinding(
    itemContainerLayoutId: Int = R.layout.flow_scroller_item_container,
    scrollMode: ScrollMode = ScrollMode.PAGER,
    recyclerViewId: Int = R.id.recyclerView,
    debounceStreamProvider: DebounceStreamProvider? = null,
    model: UIStatesScrollerModel<D, UI, S, Out>,
): UIStatesScrollerBinding<UI, S> {
    return UIStatesScrollerModelBindingImpl(
        controller = this,
        itemContainerLayoutId = itemContainerLayoutId,
        scrollMode = scrollMode,
        model = model,
        recyclerViewId = recyclerViewId,
        debounceStreamProvider = debounceStreamProvider,
    )
}