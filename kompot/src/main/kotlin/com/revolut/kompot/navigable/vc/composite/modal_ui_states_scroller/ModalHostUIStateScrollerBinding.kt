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

package com.revolut.kompot.navigable.vc.composite.modal_ui_states_scroller

import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.composite.CompositeModelBinding
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIStatesBinding
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIStatesBindingImpl
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIStatesController
import com.revolut.kompot.navigable.vc.composite.modal_ui_states.ModalHostUIStatesModel
import com.revolut.kompot.navigable.vc.scroller.ScrollMode
import com.revolut.kompot.navigable.vc.scroller.ScrollerItem
import com.revolut.kompot.navigable.vc.scroller.ScrollerModelBinding
import com.revolut.kompot.navigable.vc.scroller.ScrollerModelBindingImpl
import com.revolut.kompot.navigable.vc.scroller.ScrollerViewController
import com.revolut.kompot.navigable.vc.scroller.ScrollerViewModel
import com.revolut.kompot.navigable.vc.ui.DebounceStreamProvider
import com.revolut.kompot.navigable.vc.ui.States

interface ModalHostUIStatesScrollerBinding<UI : States.UI, S : ScrollerItem> : ScrollerModelBinding<S>, ModalHostUIStatesBinding<UI>

interface ModalHostUIStatesScroller<UI : States.UI, S : ScrollerItem> : ScrollerViewController<S>, ModalHostUIStatesController<UI> {
    override val modelBinding: ModalHostUIStatesScrollerBinding<UI, S>
}

interface ModalHostUIStatesScrollerModel<D : States.Domain, UI : States.UI, Item : ScrollerItem, MS: FlowStep, Out : IOData.Output>
    : ScrollerViewModel<Item, Out>, ModalHostUIStatesModel<D, UI, MS, Out>

internal class ModalHostUIStatesScrollerModelBindingImpl<D : States.Domain, UI : States.UI, S : ScrollerItem, MS: FlowStep, Out : IOData.Output>(
    controller: ModalHostUIStatesScroller<UI, S>,
    itemContainerLayoutId: Int,
    scrollMode: ScrollMode,
    model: ModalHostUIStatesScrollerModel<D, UI, S, MS, Out>,
    recyclerViewId: Int,
    debounceStreamProvider: DebounceStreamProvider?,
    private val scrollerModelBinding: ScrollerModelBinding<S> = ScrollerModelBindingImpl(
        controller = controller,
        itemContainerLayoutId = itemContainerLayoutId,
        scrollMode = scrollMode,
        model = model,
        recyclerViewId = recyclerViewId,
    ),
) : ModalHostUIStatesScrollerBinding<UI, S>, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        scrollerModelBinding,
        ModalHostUIStatesBindingImpl(
            controller = controller,
            model = model,
            debounceStreamProvider = debounceStreamProvider,
        ),
    )
)

@Suppress("FunctionName")
fun <D : States.Domain, UI : States.UI, S : ScrollerItem, MS: FlowStep, Out : IOData.Output> ModalHostUIStatesScroller<UI, S>.ModelBinding(
    itemContainerLayoutId: Int = R.layout.flow_scroller_item_container,
    scrollMode: ScrollMode = ScrollMode.PAGER,
    recyclerViewId: Int = R.id.recyclerView,
    debounceStreamProvider: DebounceStreamProvider? = null,
    model: ModalHostUIStatesScrollerModel<D, UI, S, MS, Out>,
): ModalHostUIStatesScrollerBinding<UI, S> {
    return ModalHostUIStatesScrollerModelBindingImpl(
        controller = this,
        itemContainerLayoutId = itemContainerLayoutId,
        scrollMode = scrollMode,
        model = model,
        recyclerViewId = recyclerViewId,
        debounceStreamProvider = debounceStreamProvider,
    )
}