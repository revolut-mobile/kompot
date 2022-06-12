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
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.ModelBinder

interface FlowModel<STEP : FlowStep, OUTPUT : IOData.Output> {
    val step: STEP

    val hasBackStack: Boolean

    val hasChildFlow: Boolean

    val animation: TransitionAnimation

    val restorationNeeded: Boolean

    fun navigationBinder(): ModelBinder<FlowNavigationCommand<STEP, OUTPUT>>

    fun getController(): Controller

    fun restorePreviousState()

    fun setNextState(
        step: STEP,
        animation: TransitionAnimation,
        addCurrentStepToBackStack: Boolean,
        childFlowModel: FlowModel<*, *>?
    )

    fun updateChildFlowState(childFlowModel: FlowModel<*, *>?)

    fun updateCurrentScreenState(state: Bundle)

    fun saveState(outState: Bundle)

    fun restoreState(restorationPolicy: RestorationPolicy)

    fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean

}