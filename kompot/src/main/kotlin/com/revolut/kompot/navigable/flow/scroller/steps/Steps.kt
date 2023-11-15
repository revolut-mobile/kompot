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

package com.revolut.kompot.navigable.flow.scroller.steps

import com.revolut.kompot.navigable.flow.scroller.ScrollerFlowStep

data class Steps<S : ScrollerFlowStep>(
    val steps: List<S>,
    val selectedStepId: String = requireNotNull(steps.firstOrNull()?.id) { "Non empty list should be provided for steps" }
) {

    companion object {
        operator fun <S : ScrollerFlowStep> invoke(vararg steps: S): Steps<S> = Steps(steps = steps.toList())
    }
}

data class StepsChangeCommand<S : ScrollerFlowStep>(
    val steps: List<S>,
    val selectedStepId: String?,
    val smoothScroll: Boolean,
)