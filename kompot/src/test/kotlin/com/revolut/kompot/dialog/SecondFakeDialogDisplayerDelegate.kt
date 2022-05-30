package com.revolut.kompot.dialog

internal class SecondFakeDialogDisplayerDelegate : DialogDisplayerDelegate<SecondFakeDialogModel>() {
    override fun canHandle(dialogModel: DialogModel<*>): Boolean = dialogModel is SecondFakeDialogModel

    override fun showDialogInternal(dialogModel: SecondFakeDialogModel) {
        //do nothing
    }

    override fun hideDialog() {
        //do nothing
    }
}

data class SecondFakeDialogModel(val message: String) : DialogModel<SecondFakeDialogModelResult>

object SecondFakeDialogModelResult : DialogModelResult