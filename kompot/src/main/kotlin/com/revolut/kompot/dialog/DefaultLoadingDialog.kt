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