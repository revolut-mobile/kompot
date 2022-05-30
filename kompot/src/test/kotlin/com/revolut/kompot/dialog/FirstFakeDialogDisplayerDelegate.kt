package com.revolut.kompot.dialog

internal class FirstFakeDialogDisplayerDelegate : DialogDisplayerDelegate<FirstFakeDialogModel>() {
    override fun canHandle(dialogModel: DialogModel<*>): Boolean = dialogModel is FirstFakeDialogModel

    override fun showDialogInternal(dialogModel: FirstFakeDialogModel) {
        //do nothing
    }

    override fun hideDialog() {
        //do nothing
    }

    fun testPostResult() = postResult(FirstFakeDialogModelResult)
}

data class FirstFakeDialogModel(val message: String) : DialogModel<FirstFakeDialogModelResult>

object FirstFakeDialogModelResult : DialogModelResult