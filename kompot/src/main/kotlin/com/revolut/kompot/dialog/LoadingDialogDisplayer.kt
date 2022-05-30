package com.revolut.kompot.dialog

import android.app.Activity
import android.os.Handler
import android.os.Looper

interface LoadingDialogDisplayer {
    fun showLoadingDialog(delayDuration: Long)

    fun hideLoadingDialog()

    fun onDestroy()
}

interface LoadingDialogFactory {
    fun createLoadingDialog(delayDuration: Long): LoadingDialog
}

interface LoadingDialog {
    fun show()

    fun dismiss()
}

class DefaultLoadingDialogFactory(
    private val activity: Activity
) : LoadingDialogFactory {
    override fun createLoadingDialog(delayDuration: Long): LoadingDialog {
        return DefaultLoadingDialog(activity, delayDuration = delayDuration)
    }
}

class DefaultLoadingDialogDisplayer(
    val activity: Activity,
    val loadingDialogFactory: LoadingDialogFactory = DefaultLoadingDialogFactory(activity)
) : LoadingDialogDisplayer {
    @Volatile
    private var loadingDialogCounter = 0
    private var loadingDialog: LoadingDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun showLoadingDialog(delayDuration: Long) {
        handler.post {
            if (loadingDialogCounter > 0) {
                loadingDialogCounter += 1
            } else {
                if (!activity.isFinishing) {
                    val dialog = loadingDialogFactory.createLoadingDialog(delayDuration)
                    loadingDialogCounter += 1
                    loadingDialog = dialog
                    dialog.show()
                }
            }
        }
    }

    override fun hideLoadingDialog() {
        handler.post {
            if (loadingDialogCounter > 1) {
                loadingDialogCounter -= 1
            } else {
                loadingDialogCounter = 0
                loadingDialog?.dismiss()
                loadingDialog = null
            }
        }
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}