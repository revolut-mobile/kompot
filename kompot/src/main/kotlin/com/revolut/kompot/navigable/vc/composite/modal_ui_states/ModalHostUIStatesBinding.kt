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

package com.revolut.kompot.navigable.vc.composite.modal_ui_states

import androidx.recyclerview.widget.RecyclerView
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.composite.CompositeModelBinding
import com.revolut.kompot.navigable.vc.modal.ModalHostBinding
import com.revolut.kompot.navigable.vc.modal.ModalHostBindingImpl
import com.revolut.kompot.navigable.vc.modal.ModalHostController
import com.revolut.kompot.navigable.vc.modal.ModalHostViewModel
import com.revolut.kompot.navigable.vc.ui.DebounceStreamProvider
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStateModelBindingImpl
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import com.revolut.kompot.navigable.vc.ui.UIStatesModelBinding
import com.revolut.kompot.navigable.vc.ui.list.DefaultAdapter
import com.revolut.kompot.navigable.vc.ui.list.DefaultLayoutManager
import com.revolut.kompot.navigable.vc.ui.list.LayoutManagerProvider
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesController
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesModel
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesModelBinding
import com.revolut.kompot.navigable.vc.ui.list.UIListStatesModelBindingImpl
import com.revolut.recyclerkit.delegates.DiffAdapter
import com.revolut.recyclerkit.delegates.RecyclerViewDelegate

interface ModalHostUIStatesBinding<UI : States.UI> : UIStatesModelBinding<UI>,
    ModalHostBinding

interface ModalHostUIListStatesBinding<UI : States.UIList> : UIListStatesModelBinding<UI>,
    ModalHostBinding

interface ModalHostUIStatesController<UI : States.UI> : UIStatesController<UI>,
    ModalHostController {
    override val modelBinding: ModalHostUIStatesBinding<UI>
}

interface ModalHostUIListStatesController<UI : States.UIList> : UIListStatesController<UI>,
    ModalHostController {
    override val modelBinding: ModalHostUIListStatesBinding<UI>
}

interface ModalHostUIStatesModel<D : States.Domain, UI : States.UI, S : FlowStep, Out : IOData.Output> :
    UIStatesModel<D, UI, Out>,
    ModalHostViewModel<S, Out>

interface ModalHostUIListStatesModel<D : States.Domain, UI : States.UIList, S : FlowStep, Out : IOData.Output> :
    UIListStatesModel<D, UI, Out>,
    ModalHostViewModel<S, Out>

internal class ModalHostUIStatesBindingImpl<D : States.Domain, UI : States.UI, S : FlowStep, Out : IOData.Output>(
    controller: ModalHostUIStatesController<UI>,
    model: ModalHostUIStatesModel<D, UI, S, Out>,
    debounceStreamProvider: DebounceStreamProvider?,
) : ModalHostUIStatesBinding<UI>, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        UIStateModelBindingImpl(
            controller = controller,
            model = model,
            debounceStreamProvider = debounceStreamProvider,
        ),
        ModalHostBindingImpl(
            controller = controller,
            model = model
        )
    )
)

internal class ModalHostUIListStatesBindingImpl<D : States.Domain, UI : States.UIList, S : FlowStep, Out : IOData.Output>(
    controller: ModalHostUIListStatesController<UI>,
    model: ModalHostUIListStatesModel<D, UI, S, Out>,
    delegates: List<RecyclerViewDelegate<*, *>>,
    recyclerViewId: Int,
    layoutManagerProvider: LayoutManagerProvider,
    listAdapter: DiffAdapter,
    debounceStreamProvider: DebounceStreamProvider?,
    private val uiListStatesModelBinding: UIListStatesModelBinding<UI> = UIListStatesModelBindingImpl(
        controller = controller,
        model = model,
        delegates = delegates,
        recyclerViewId = recyclerViewId,
        layoutManagerProvider = layoutManagerProvider,
        listAdapter = listAdapter,
        debounceStreamProvider = debounceStreamProvider,
    ),
) : ModalHostUIListStatesBinding<UI>, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        uiListStatesModelBinding,
        ModalHostBindingImpl(
            controller = controller,
            model = model
        )
    )
) {
    override val recyclerView: RecyclerView get() = uiListStatesModelBinding.recyclerView
    override val layoutManager: RecyclerView.LayoutManager get() = uiListStatesModelBinding.layoutManager
}

@Suppress("FunctionName")
fun <D : States.Domain, UI : States.UI, S : FlowStep, Out : IOData.Output> ModalHostUIStatesController<UI>.ModelBinding(
    model: ModalHostUIStatesModel<D, UI, S, Out>,
    debounceStreamProvider: DebounceStreamProvider? = null,
): ModalHostUIStatesBinding<UI> {
    return ModalHostUIStatesBindingImpl(
        controller = this,
        model = model,
        debounceStreamProvider = debounceStreamProvider,
    )
}

@Suppress("FunctionName")
fun <D : States.Domain, UI : States.UIList, S : FlowStep, Out : IOData.Output> ModalHostUIListStatesController<UI>.ModelBinding(
    model: ModalHostUIListStatesModel<D, UI, S, Out>,
    delegates: List<RecyclerViewDelegate<*, *>>,
    recyclerViewId: Int = R.id.recyclerView,
    layoutManagerProvider: LayoutManagerProvider = { DefaultLayoutManager(this) },
    listAdapter: DiffAdapter = DefaultAdapter(),
    debounceStreamProvider: DebounceStreamProvider? = null,
): ModalHostUIListStatesBinding<UI> {
    return ModalHostUIListStatesBindingImpl(
        controller = this,
        model = model,
        delegates = delegates,
        recyclerViewId = recyclerViewId,
        layoutManagerProvider = layoutManagerProvider,
        listAdapter = listAdapter,
        debounceStreamProvider = debounceStreamProvider,
    )
}