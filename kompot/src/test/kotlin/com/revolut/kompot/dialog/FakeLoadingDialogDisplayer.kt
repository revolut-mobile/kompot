package com.revolut.kompot.dialog

object FakeLoadingDialogDisplayer : LoadingDialogDisplayer {
    override fun showLoadingDialog(delayDuration: Long) = Unit

    override fun hideLoadingDialog() = Unit

    override fun onDestroy() = Unit
}