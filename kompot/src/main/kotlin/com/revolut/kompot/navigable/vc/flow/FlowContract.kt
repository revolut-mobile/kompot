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

package com.revolut.kompot.navigable.vc.flow

import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi

interface FlowModelBinding : ModelBinding {
    val defaultFlowLayoutId: Int
    val hasBackStack: Boolean
}

interface FlowViewController : ViewControllerApi, ParentFlow {
    override val layoutId: Int get() = modelBinding.defaultFlowLayoutId
    override val modelBinding: FlowModelBinding
    override val hasBackStack: Boolean get() = modelBinding.hasBackStack
}

interface FlowViewModel<S : FlowStep, Out : IOData.Output> : ViewControllerModelApi<Out> {
    val flowCoordinator: FlowCoordinator<S, Out>
}