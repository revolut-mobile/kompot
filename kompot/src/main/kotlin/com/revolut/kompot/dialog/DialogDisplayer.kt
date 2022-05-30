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