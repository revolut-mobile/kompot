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

import android.os.Bundle
import android.os.Parcelable
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.TransitionAnimation
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class FlowStateWrapper<STATE : FlowState, STEP : FlowStep>(
    val state: STATE,
    val step: STEP,
    val childFlowState: ChildFlowState? = null,
    val currentScreenState: Bundle? = null,
    val animation: TransitionAnimation = TransitionAnimation.NONE,
    val currentControllerKey: ControllerKey? = null
): Parcelable

@Parcelize
internal data class ChildFlowState(
    val stateWrapper: FlowStateWrapper<*, *>,
    val backStack: List<FlowStateWrapper<*, *>>,
) : Parcelable