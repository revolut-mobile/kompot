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

package com.revolut.kompot.navigable.flow

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.doOnPreDraw
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.service.ScreenAddedEvent
import com.revolut.kompot.common.service.ServiceEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.transition.AnimatorTransition

internal class FlowServiceEventHandler(
    private val controller: Controller,
    private val controllerModel: ControllerModel,
    private val parentController: Controller?,
    private val parentControllerManager: ControllerManager,
    private val view: View
) {

    private var initialBackgroundColor: Int? = (view.background as? ColorDrawable)?.color

    fun handleEvent(event: Event): EventResult? {
        if (event._controller == null) {
            event._controller = controller
        }

        if (event is ServiceEvent) {
            if (handleServiceEvent(event)) {
                return null
            }
            return (parentController as? EventsDispatcher)?.handleEvent(event)
        }

        return controllerModel.tryHandleEvent(event) ?: (parentController as? EventsDispatcher)?.handleEvent(event)
    }

    private fun handleServiceEvent(serviceEvent: ServiceEvent): Boolean {
        when (serviceEvent) {
            is ScreenAddedEvent -> {
                handleScreenAddedEvent(serviceEvent)
                if (parentControllerManager.modal) {
                    return true
                }
            }
        }

        return false
    }

    private fun handleScreenAddedEvent(event: ScreenAddedEvent) {
        if (initialBackgroundColor != null) {
            val screenBackground = event.screen.view.background
            if (screenBackground is ColorDrawable) {
                changeBackgroundColor(screenBackground.color, event.animated || event.parentFlow != controller)
            } else {
                initialBackgroundColor?.run { changeBackgroundColor(this, event.animated || event.parentFlow != controller) }
            }
        }
    }

    private fun changeBackgroundColor(@ColorInt color: Int, animated: Boolean) {
        if (!animated) {
            view.setBackgroundColor(color)
            return
        }

        val currentColor = (view.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
        if (currentColor != color) {
            view.doOnPreDraw {
                ValueAnimator.ofObject(ArgbEvaluator(), currentColor, color).apply {
                    duration = AnimatorTransition.DURATION_DEFAULT
                    addUpdateListener { animator ->
                        view.setBackgroundColor(animator.animatedValue as Int)
                    }
                    start()
                }
            }
        }
    }
}