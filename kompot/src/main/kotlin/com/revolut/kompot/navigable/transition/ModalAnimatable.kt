package com.revolut.kompot.navigable.transition

import android.view.View
import androidx.annotation.FloatRange

interface ModalAnimatable {

    val view: View

    var style: Style

    fun show(onTransitionEnd: () -> Unit)

    fun hide(onTransitionEnd: () -> Unit)

    fun addContent(view: View)

    fun removeContent(view: View)

    fun setOnDismissListener(onDismiss: (() -> Unit)?)

    enum class Style {
        FADE, SLIDE
    }
}