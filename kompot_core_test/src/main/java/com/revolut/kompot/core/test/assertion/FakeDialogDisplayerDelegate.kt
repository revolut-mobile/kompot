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

package com.revolut.kompot.core.test.assertion

import com.revolut.kompot.dialog.DialogDisplayerDelegate
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult

internal class FakeDialogDisplayerDelegate(
    private val dialogResultStream: kotlinx.coroutines.flow.Flow<DialogModelResult>,
    private val onShown: (DialogModel<*>) -> Unit
) : DialogDisplayerDelegate<DialogModel<*>>() {

    override fun canHandle(dialogModel: DialogModel<*>) = true

    override fun showDialogInternal(dialogModel: DialogModel<*>) {
        onShown(dialogModel)
    }

    override fun hideDialog() {
        //do nothing
    }

    override fun startObservingResult(): kotlinx.coroutines.flow.Flow<DialogModelResult> = dialogResultStream
}