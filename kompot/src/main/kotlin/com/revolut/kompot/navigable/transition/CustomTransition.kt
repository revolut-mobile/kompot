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

package com.revolut.kompot.navigable.transition

import android.view.View
import com.revolut.kompot.navigable.TransitionAnimation

internal class CustomTransition(
    private val animation: TransitionAnimation.Custom
) : Transition {

    private var transitionOwner: CustomTransitionOwner? = null

    override fun start(
        from: View?,
        to: View?,
        backward: Boolean,
        transitionListener: TransitionListener
    ) {
        transitionOwner = (to ?: from)?.let(::getTransitionOwner)
        transitionOwner?.setTransitionListener(transitionListener)
        transitionOwner?.startTransition(backward, animation)
    }

    override fun endImmediately() {
        transitionOwner?.setTransitionListener(null)
        transitionOwner = null
    }

    private fun getTransitionOwner(view: View): CustomTransitionOwner {
        view.rootView.findViewById<View>(animation.providerId)?.let { provider ->
            return provider as? CustomTransitionOwner
                ?: error(
                    "CustomTransitionOwner with id ${animation.providerId} must implement CustomTransitionOwner interface"
                )
        } ?: error(
            "CustomTransitionOwner with id ${animation.providerId} not found in view hierarchy"
        )
    }
}

interface CustomTransitionOwner {
    fun startTransition(backward: Boolean, animation: TransitionAnimation.Custom)
    fun setTransitionListener(transitionListener: TransitionListener?)
}