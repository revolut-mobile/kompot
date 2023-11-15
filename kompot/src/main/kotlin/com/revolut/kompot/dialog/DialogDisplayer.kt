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

package com.revolut.kompot.dialog

import kotlinx.coroutines.flow.Flow

class DialogDisplayer(
    private val loadingDialogDisplayer: LoadingDialogDisplayer,
    private val delegates: List<DialogDisplayerDelegate<*>>
) {
    fun showLoadingDialog(delayDuration: Long) = loadingDialogDisplayer.showLoadingDialog(delayDuration)

    fun hideLoadingDialog() = loadingDialogDisplayer.hideLoadingDialog()

    fun <Result : DialogModelResult> showDialog(dialogModel: DialogModel<Result>): Flow<Result> = delegates
        .find { displayer -> displayer.canHandle(dialogModel) }
        ?.showDialog(dialogModel)
        ?: throw IllegalStateException("No displayer delegate found for ${dialogModel.javaClass}")

    fun hideAllDialogs() = delegates.forEach { it.hideDialog() }

    fun hideDialog(dialogModel: DialogModel<*>) {
        delegates
            .find { displayer -> displayer.canHandle(dialogModel) }
            ?.hideDialog()
            ?: throw IllegalStateException("No displayer delegate found for ${dialogModel.javaClass}")
    }

    fun onAttach() {
        delegates.forEach(DialogDisplayerDelegate<*>::onAttach)
    }

    fun onDetach() {
        delegates.forEach(DialogDisplayerDelegate<*>::onDetach)
    }

    fun onCreate() {
        delegates.forEach(DialogDisplayerDelegate<*>::onCreate)
    }

    fun onDestroy() {
        loadingDialogDisplayer.onDestroy()

        delegates.forEach(DialogDisplayerDelegate<*>::onDestroy)
    }
}