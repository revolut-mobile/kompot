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

package com.revolut.kompot.navigable.vc.modal

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi

interface ModalHostController : ViewControllerApi {
    override val modelBinding: ModalHostBinding
}

interface ModalHostViewModel<S : FlowStep, Out : IOData.Output> : ViewControllerModelApi<Out> {
    val modalCoordinator: ModalCoordinator<S, Out>
}