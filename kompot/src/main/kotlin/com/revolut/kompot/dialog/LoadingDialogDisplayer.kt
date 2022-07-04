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