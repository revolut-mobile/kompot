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

package com.revolut.kompot.navigable.vc

import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.TransitionAnimation
import com.revolut.kompot.navigable.binder.ModelBinder
import com.revolut.kompot.navigable.flow.FlowStep
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi
import com.revolut.kompot.navigable.vc.common.StateHolder
import com.revolut.kompot.navigable.vc.flow.FlowCoordinator
import com.revolut.kompot.navigable.vc.modal.ModalCoordinator
import com.revolut.kompot.navigable.vc.scroller.ScrollerCoordinator
import com.revolut.kompot.navigable.vc.scroller.ScrollerItem

abstract class ViewControllerModel<OUTPUT : IOData.Output> : ControllerModel(), ViewControllerModelApi<OUTPUT> {

    private val resultCommandsBinder = ModelBinder<OUTPUT>()
    private val backCommandsBinder = ModelBinder<Unit>()

    override fun resultsBinder(): ModelBinder<OUTPUT> = resultCommandsBinder
    override fun backPressBinder(): ModelBinder<Unit> = backCommandsBinder

    protected fun postResult(result: OUTPUT) {
        resultCommandsBinder.notify(result)
    }

    fun postBack() {
        backCommandsBinder.notify(Unit)
    }

    protected fun <S> StateHolder<S>.update(func: S.() -> S) = update(func)

    protected fun <Step : FlowStep> FlowCoordinator<Step, *>.next(
        step: Step,
        addCurrentStepToBackStack: Boolean,
        animation: TransitionAnimation = TransitionAnimation.SLIDE_RIGHT_TO_LEFT,
        executeImmediately: Boolean = false,
    ) = next(
        step = step,
        addCurrentStepToBackStack = addCurrentStepToBackStack,
        animation = animation,
        executeImmediately = executeImmediately,
    )

    protected fun <S : ScrollerItem> ScrollerCoordinator<S>.updateItems(
        selectedItemId: String? = null,
        items: List<S>,
        smoothScroll: Boolean = true,
    ) = updateItems(
        selectedItemId = selectedItemId,
        items = items,
        smoothScroll = smoothScroll,
    )

    protected fun <S : ScrollerItem> ScrollerCoordinator<S>.updateItems(
        selectedItemId: String? = null,
        smoothScroll: Boolean = true,
    ) = updateItems(
        selectedItemId = selectedItemId,
        smoothScroll = smoothScroll,
    )

    protected fun FlowCoordinator<*, *>.quit() = quit()
    protected fun ScrollerCoordinator<*>.quit() = quit()
    protected fun FlowCoordinator<*, *>.clearBackStack() = clearBackStack()
    protected fun <Step : FlowStep> FlowCoordinator<Step, *>.openModal(step: Step) = openModal(step)
    protected fun <Step : FlowStep> ModalCoordinator<Step, *>.openModal(step: Step) = openModal(step)
}