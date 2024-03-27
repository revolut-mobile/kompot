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

package com.revolut.kompot.navigable.vc.composite.ui_states_flow

import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.composite.CompositeModelBinding
import com.revolut.kompot.navigable.vc.flow.FlowModelBinding
import com.revolut.kompot.navigable.vc.flow.FlowModelBindingImpl
import com.revolut.kompot.navigable.vc.flow.FlowViewController
import com.revolut.kompot.navigable.vc.flow.FlowViewModel
import com.revolut.kompot.navigable.vc.ui.DebounceStreamProvider
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStateModelBindingImpl
import com.revolut.kompot.navigable.vc.ui.UIStatesController
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import com.revolut.kompot.navigable.vc.ui.UIStatesModelBinding

interface UIStatesFlowBinding<UI : States.UI> : FlowModelBinding, UIStatesModelBinding<UI>

interface UIStatesFlowController<UI : States.UI> : FlowViewController,
    UIStatesController<UI> {
    override val modelBinding: UIStatesFlowBinding<UI>
}

interface UIStatesFlowModel<D : States.Domain, UI : States.UI, Step : FlowStep, Out : IOData.Output>
    : FlowViewModel<Step, Out>, UIStatesModel<D, UI, Out>

internal class UIStatesFlowModelBindingImpl<D : States.Domain, UI : States.UI, S : FlowStep, Out : IOData.Output>(
    containerId: Int,
    controller: UIStatesFlowController<UI>,
    model: UIStatesFlowModel<D, UI, S, Out>,
    debounceStreamProvider: DebounceStreamProvider?,
    private val flowModelBinding: FlowModelBinding = FlowModelBindingImpl(
        containerId = containerId,
        controller = controller,
        model = model,
    ),
) : UIStatesFlowBinding<UI>, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        flowModelBinding,
        UIStateModelBindingImpl(
            controller = controller,
            model = model,
            debounceStreamProvider = debounceStreamProvider,
        ),
    )
) {
    override val hasBackStack: Boolean get() = flowModelBinding.hasBackStack
}

@Suppress("FunctionName")
fun <D : States.Domain, UI : States.UI, S : FlowStep, Out : IOData.Output> UIStatesFlowController<UI>.ModelBinding(
    model: UIStatesFlowModel<D, UI, S, Out>,
    containerId: Int = R.id.container,
    debounceStreamProvider: DebounceStreamProvider? = null,
): UIStatesFlowBinding<UI> {
    return UIStatesFlowModelBindingImpl(
        controller = this,
        containerId = containerId,
        model = model,
        debounceStreamProvider = debounceStreamProvider,
    )
}