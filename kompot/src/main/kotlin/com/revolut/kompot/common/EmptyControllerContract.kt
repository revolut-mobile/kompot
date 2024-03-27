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

package com.revolut.kompot.common

import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.screen.StateMapper

class EmptyStateMapper : StateMapper<ScreenStates.EmptyDomain, ScreenStates.EmptyUI> {
    override fun mapState(domainState: ScreenStates.EmptyDomain) = ScreenStates.EmptyUI
}

class EmptyListStateMapper : StateMapper<ScreenStates.EmptyDomain, ScreenStates.EmptyUIList> {
    override fun mapState(domainState: ScreenStates.EmptyDomain) = ScreenStates.EmptyUIList
}

class EmptyScreenModel<Output : IOData.Output> : BaseScreenModel<ScreenStates.EmptyDomain, ScreenStates.EmptyUI, Output>(EmptyStateMapper()) {
    override val initialState = ScreenStates.EmptyDomain
}

class EmptyListScreenModel : BaseScreenModel<ScreenStates.EmptyDomain, ScreenStates.EmptyUIList, IOData.EmptyOutput>(EmptyListStateMapper()) {
    override val initialState = ScreenStates.EmptyDomain
}