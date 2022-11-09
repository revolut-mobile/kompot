package com.revolut.kompot.navigable.root

import com.revolut.kompot.navigable.utils.Preconditions
import com.revolut.kompot.utils.ControllerScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

internal class NavActionsScheduler {

    private val scope = ControllerScope(Dispatchers.Main)
    private val pendingJobs = mutableSetOf<Job>()

    fun schedule(tag: String, action: suspend () -> Unit) {
        Preconditions.requireMainThread("NavActionsScheduler")
        val job = scope.launch(CoroutineName(tag)) {
            coroutineContext[Job]?.let(pendingJobs::remove)
            action()
        }
        pendingJobs += job
        job.invokeOnCompletion { pendingJobs -= job }
    }

    fun hasPendingActions(): Boolean {
        Preconditions.requireMainThread("NavActionsScheduler")
        return pendingJobs.isNotEmpty()
    }

    fun cancel(tag: String) {
        scope.coroutineContext[Job]?.children?.forEach { child ->
            val childName = (child as? CoroutineScope)?.coroutineContext?.get(CoroutineName.Key)?.name ?: return@forEach
            if (childName == tag) child.cancel()
        }
    }

    fun cancelAll() {
        scope.coroutineContext.cancelChildren()
    }

}