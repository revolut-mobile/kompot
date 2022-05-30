package com.revolut.kompot.dialog

import android.app.Dialog
import androidx.annotation.CallSuper
import com.revolut.kompot.utils.DEFAULT_EXTRA_BUFFER_CAPACITY
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.takeWhile

abstract class DialogDisplayerDelegate<Model : DialogModel<*>> {
    private val dialogs = mutableListOf<Dialog>()

    private val events = MutableSharedFlow<DialogDisplayerEvent>(
        extraBufferCapacity = DEFAULT_EXTRA_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun <Result : DialogModelResult> showDialog(dialogModel: DialogModel<*>): Flow<Result> {
        showDialogInternal(dialogModel as Model)
        return startObservingResult().map { it as Result }
    }

    abstract fun hideDialog()

    abstract fun canHandle(dialogModel: DialogModel<*>): Boolean

    protected abstract fun showDialogInternal(dialogModel: Model)

    protected open fun startObservingResult(): Flow<DialogModelResult> {
        events.tryEmit(DialogDisplayerEvent.Finish)
        return events
            .takeWhile { it !is DialogDisplayerEvent.Finish }
            .mapNotNull { (it as? DialogDisplayerEvent.PostResult)?.modelResult }
    }

    protected fun postResult(result: DialogModelResult) {
        events.tryEmit(DialogDisplayerEvent.PostResult(result))
    }

    open fun onAttach() = Unit

    open fun onDetach() = Unit

    open fun onCreate() = Unit

    @CallSuper
    open fun onDestroy() {
        dialogs.forEach { it.dismiss() }
        dialogs.clear()
    }

    protected fun addDialog(dialog: Dialog) {
        dialogs += dialog
    }

    protected fun removeDialog(dialog: Dialog) {
        dialogs -= dialog
    }
}

private sealed class DialogDisplayerEvent {
    data class PostResult(val modelResult: DialogModelResult) : DialogDisplayerEvent()
    object Finish : DialogDisplayerEvent()
}