package com.revolut.kompot.navigable.utils.single_task

import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class SingleTasksRegistry {

    private val tasksRegistry by lazy {
        Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    }

    fun acquire(taskId: String): Boolean = tasksRegistry.add(taskId)

    fun release(taskId: String) {
        tasksRegistry.remove(taskId)
    }

}