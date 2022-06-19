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

package com.revolut.kompot.navigable.flow

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.TransitionAnimation

sealed class FlowNavigationCommand<STEP : FlowStep, OUTPUT : IOData.Output>

data class Next<STEP : FlowStep, OUTPUT : IOData.Output>(val step: STEP, val addCurrentStepToBackStack: Boolean, val animation: TransitionAnimation) :
    FlowNavigationCommand<STEP, OUTPUT>()

class Back<STEP : FlowStep, OUTPUT : IOData.Output> : FlowNavigationCommand<STEP, OUTPUT>()

class Quit<STEP : FlowStep, OUTPUT : IOData.Output> : FlowNavigationCommand<STEP, OUTPUT>()

data class PostFlowResult<STEP : FlowStep, OUTPUT : IOData.Output>(val data: OUTPUT) : FlowNavigationCommand<STEP, OUTPUT>()

class StartPostponedStateRestore<STEP : FlowStep, OUTPUT : IOData.Output>: FlowNavigationCommand<STEP, OUTPUT>()