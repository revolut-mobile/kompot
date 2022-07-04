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

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.revolut.kompot.R

internal class DefaultLoadingDialog(
    private val activity: Activity,
    private val delayDuration: Long
) : Dialog(activity, R.style.LoadingDialog), LoadingDialog {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.systemUiVisibility = activity.window.decorView.systemUiVisibility

        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        setContentView(contentView)

        setOnShowListener {
            ObjectAnimator.ofFloat(contentView, View.ALPHA, 0f, 1f).setDuration(delayDuration)
                .apply {
                    setInterpolator { v -> if (v < 0.5f) 0f else ((v - 0.5) * 2.0 * (v - 0.5) * 2.0).toFloat() }
                    start()
                }
        }

        setCancelable(false)
    }
}