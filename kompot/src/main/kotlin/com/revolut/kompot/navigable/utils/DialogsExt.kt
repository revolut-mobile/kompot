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

package com.revolut.kompot.navigable.utils

import com.revolut.kompot.dialog.DialogDisplayer
import com.revolut.kompot.dialog.DialogModel
import com.revolut.kompot.dialog.DialogModelResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

fun setBlockingLoadingVisibility(dialogDisplayer: DialogDisplayer, visible: Boolean, immediate: Boolean = false) {
    if (visible) {
        dialogDisplayer.showLoadingDialog(if (immediate) 0 else 1000)
    } else {
        dialogDisplayer.hideLoadingDialog()
    }
}

internal suspend fun <T> withLoading(
    dialogDisplayer: DialogDisplayer,
    mainDispatcher: CoroutineDispatcher,
    block: suspend () -> T,
): T = withContext(mainDispatcher) {
    try {
        setBlockingLoadingVisibility(dialogDisplayer, true)
        block()
    } finally {
        setBlockingLoadingVisibility(dialogDisplayer, false)
    }
}

internal fun <Result : DialogModelResult> showDialog(dialogDisplayer: DialogDisplayer, dialogModel: DialogModel<Result>): kotlinx.coroutines.flow.Flow<Result> =
    dialogDisplayer.showDialog(dialogModel)

internal fun hideDialog(dialogDisplayer: DialogDisplayer, dialogModel: DialogModel<*>) {
    dialogDisplayer.hideDialog(dialogModel)
}

internal fun hideAllDialogs(dialogDisplayer: DialogDisplayer) = dialogDisplayer.hideAllDialogs()