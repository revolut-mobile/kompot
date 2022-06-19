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

package com.revolut.kompot.navigable.screen

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.postDelayed
import com.revolut.kompot.common.Event
import com.revolut.kompot.common.EventResult
import com.revolut.kompot.common.EventsDispatcher
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.LifecycleEvent
import com.revolut.kompot.di.flow.ParentFlowComponent
import com.revolut.kompot.di.screen.BaseScreenComponent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import com.revolut.kompot.navigable.binder.CompositeBinding
import com.revolut.kompot.navigable.findRootFlow
import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.utils.DEFAULT_EXTRA_BUFFER_CAPACITY
import com.revolut.kompot.utils.debounceButEmitFirst
import com.revolut.kompot.utils.withPrevious
import com.revolut.kompot.view.ControllerContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take

abstract class BaseScreen<
        UI_STATE : ScreenStates.UI,
        INPUT_DATA : IOData.Input,
        OUTPUT_DATA : IOData.Output
        >(val inputData: INPUT_DATA) : Controller(), Screen<OUTPUT_DATA>, EventsDispatcher {

    final override var onScreenResult: (data: OUTPUT_DATA) -> Unit = { }

    private val bindScreenFlow = MutableSharedFlow<UI_STATE>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val transitionEndFlow = MutableSharedFlow<Boolean>(extraBufferCapacity = DEFAULT_EXTRA_BUFFER_CAPACITY)

    private val tillDestroyBinding = CompositeBinding()

    abstract val screenComponent: BaseScreenComponent

    protected val flowComponent: ParentFlowComponent
        get() = parentFlow.component

    protected abstract val screenModel: ScreenModel<UI_STATE, OUTPUT_DATA>

    open val needKeyboard: Boolean = false

    override val controllerDelegates by lazy {
        screenComponent.getControllerExtensions()
    }

    override fun createView(inflater: LayoutInflater): View {
        val view = patchLayoutInflaterWithTheme(inflater).inflate(layoutId, null, false) as? ControllerContainer
            ?: throw IllegalStateException("Root ViewGroup should be ControllerContainer")

        view.fitStatusBar = fitStatusBar
        this.view = view as View

        view.tag = controllerName

        return view
    }

    final override fun onCreate() {
        super.onCreate()

        tillDestroyBinding += screenModel.resultsBinder()
            .bind(::onPostScreenResult)

        (screenModel as ControllerModel).injectDependencies(
            dialogDisplayer = findRootFlow().rootDialogDisplayer,
            eventsDispatcher = this@BaseScreen,
            controllersCache = controllersCache,
            mainDispatcher = Dispatchers.Main.immediate
        )

        startCollectingUIState()

        tillDestroyBinding += screenModel.backPressBinder()
            .bind { onPostBack() }

        onScreenViewCreated(view)
        (screenModel as ControllerModel).onLifecycleEvent(LifecycleEvent.CREATED)
    }

    private fun startCollectingUIState() {
        val debouncedUIStateStream = debounceStream()?.let { flow ->
            combine(
                flow.onStart { emit(Unit) },
                screenModel.uiStateStream()
            ) { _, uiState ->
                uiState
            }.debounceButEmitFirst(300)
        }

        (debouncedUIStateStream ?: screenModel.uiStateStream())
            .onEach(bindScreenFlow::emit)
            .launchIn(createdScope)
    }

    final override fun onDestroy() {
        super.onDestroy()
        tillDestroyBinding.clear()

        (screenModel as ControllerModel).onLifecycleEvent(LifecycleEvent.FINISHED)
        onScreenViewDestroyed()
    }

    open fun debounceStream(): Flow<Any>? = null

    final override fun onAttach() {
        (screenModel as ControllerModel).onLifecycleEvent(LifecycleEvent.SHOWN)
        super.onAttach()

        bindUIState()
        onScreenViewAttached(view)

        if (needKeyboard) {
            view.postDelayed(400L) {
                if (attached) {
                    activity.currentFocus?.showKeyboard()
                } else {
                    activity.currentFocus?.clearFocus()
                    view.showKeyboard(400)
                }
            }
        } else {
            activity.currentFocus?.clearFocus()
            view.showKeyboard(400)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bindUIState() {
        bindScreenFlow
            .flatMapLatest { state ->
                if (activeTransition == ActiveTransition.NONE) {
                    flowOf(state)
                } else {
                    transitionEndFlow
                        .filter { enter -> enter }
                        .take(1)
                        .map { state }
                }
            }
            .withPrevious()
            .onEach { (prevState, state) ->
                bindScreen(state, prevState?.let(state::calculatePayload))
            }
            .launchIn(attachedScope)
    }

    final override fun onDetach() {
        super.onDetach()

        (screenModel as ControllerModel).onLifecycleEvent(LifecycleEvent.HIDDEN)
        onScreenViewDetached()
    }

    private fun onPostScreenResult(result: OUTPUT_DATA) {
        Preconditions.requireMainThread("BaseScreenModel.postScreenResult()")
        onScreenResult.invoke(result)
    }

    private fun onPostBack() {
        Preconditions.requireMainThread("BaseScreenModel.postBack()")
        getTopFlow().handleBack()
    }

    override fun onTransitionRunUp(enter: Boolean) {
        super.onTransitionRunUp(enter)

        if (enter && !needKeyboard) {
            activity.currentFocus?.hideKeyboard()
        }
    }

    override fun onTransitionStart(enter: Boolean) {
        super.onTransitionStart(enter)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onTransitionEnd(enter: Boolean) {
        super.onTransitionEnd(enter)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        transitionEndFlow.tryEmit(enter)
    }

    override fun handleEvent(event: Event): EventResult? {
        if (event._controller == null) {
            event._controller = this
        }
        return (screenModel as ControllerModel).tryHandleEvent(event) ?: (parentController as? EventsDispatcher)?.handleEvent(event)
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        doOnAttach {
            onActivityResultInternal(requestCode, resultCode, data)
        }
    }

    open fun onActivityResultInternal(requestCode: Int, resultCode: Int, data: Intent?) = Unit

    final override fun onHostStarted() {
        super.onHostStarted()

        onAttach()
    }

    final override fun onHostStopped() {
        super.onHostStopped()

        onDetach()
    }

    fun saveState() = screenModel.saveState()

    fun restoreState(state: Bundle) = screenModel.restoreState(state)

    protected open fun onScreenViewAttached(view: View) = Unit

    protected open fun onScreenViewDetached() = Unit

    protected open fun onScreenViewCreated(view: View) = Unit

    protected open fun onScreenViewDestroyed() = Unit

    protected abstract fun bindScreen(uiState: UI_STATE, payload: ScreenStates.UIPayload?)
}

fun BaseScreen<*, *, *>.fitIfAncestorDoesNot(): Boolean {
    fun ViewParent.fitStatusBar(): Boolean? = (this as? ControllerContainer)?.fitStatusBar.takeIf { it == true } ?: parent?.fitStatusBar()

    return parentControllerManager.controllerViewHolder.container.fitStatusBar()?.not() ?: true
}

private fun View.showKeyboard(delayMs: Long = 0) {
    postDelayed({
        val inputMethodManager = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, delayMs)
}

private fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}