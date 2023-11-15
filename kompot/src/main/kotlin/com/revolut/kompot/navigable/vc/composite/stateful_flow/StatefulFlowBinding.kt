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

package com.revolut.kompot.navigable.vc.composite.stateful_flow

import android.os.Parcelable
import com.revolut.kompot.R
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.common.PersistableStateController
import com.revolut.kompot.navigable.vc.common.PersistableStateModel
import com.revolut.kompot.navigable.vc.common.PersistableStateModelBinding
import com.revolut.kompot.navigable.vc.composite.CompositeModelBinding
import com.revolut.kompot.navigable.vc.flow.FlowViewController
import com.revolut.kompot.navigable.vc.flow.FlowModelBinding
import com.revolut.kompot.navigable.vc.flow.FlowModelBindingImpl
import com.revolut.kompot.navigable.vc.common.PersistableStateBindingImpl
import com.revolut.kompot.navigable.vc.flow.FlowViewModel

interface StatefulFlowBinding : FlowModelBinding, PersistableStateModelBinding

interface StatefulFlowViewController : FlowViewController, PersistableStateController {
    override val modelBinding: StatefulFlowBinding
}

interface StatefulFlowModel<State : Parcelable, Step : FlowStep, Out : IOData.Output>
    : FlowViewModel<Step, Out>, PersistableStateModel<State, Out>

internal class StatefulFlowBindingImpl<State : Parcelable, S : FlowStep, Out : IOData.Output>(
    containerId: Int = R.id.container,
    controller: StatefulFlowViewController,
    model: StatefulFlowModel<State, S, Out>,
    private val flowModelBinding: FlowModelBinding = FlowModelBindingImpl(
        containerId = containerId,
        controller = controller,
        model = model,
    ),
) : StatefulFlowBinding, ModelBinding by CompositeModelBinding(
    bindings = listOf(
        flowModelBinding,
        PersistableStateBindingImpl(
            controller = controller,
            model = model
        )
    )
) {
    override val defaultFlowLayoutId: Int get() = flowModelBinding.defaultFlowLayoutId
    override val hasBackStack: Boolean get() = flowModelBinding.hasBackStack
}

@Suppress("FunctionName")
fun <State : Parcelable, S : FlowStep, Out : IOData.Output> StatefulFlowViewController.ModelBinding(
    model: StatefulFlowModel<State, S, Out>,
): StatefulFlowBinding {
    return StatefulFlowBindingImpl(
        controller = this,
        model = model,
    )
}