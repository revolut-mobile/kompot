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

package com.revolut.kompot.navigable.screen

import android.os.Bundle
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.binder.ModelBinder
import kotlinx.coroutines.flow.Flow

interface ScreenModel<UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> {

    fun uiStateStream(): Flow<UI_STATE>

    fun resultsBinder(): ModelBinder<OUTPUT>

    fun backPressBinder(): ModelBinder<Unit>

    fun saveState(): Bundle

    fun restoreState(state: Bundle)
}