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

package com.revolut.kompot.navigable.vc.test

import androidx.annotation.VisibleForTesting
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.ViewControllerModel
import com.revolut.kompot.navigable.vc.ui.States
import com.revolut.kompot.navigable.vc.ui.UIStatesModel
import kotlinx.coroutines.flow.Flow

@VisibleForTesting
fun <T, Domain : States.Domain, UI : States.UI, Output : IOData.Output> T.testDomainStateStream()
        : Flow<Domain> where T : UIStatesModel<Domain, UI, Output>,
                             T : ViewControllerModel<Output> =
    this.state.domainStateStream()