package com.revolut.kompot.navigable.root

import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class NavActionsSchedulerTest {

    private val actionsScheduler = NavActionsScheduler()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Preconditions.mainThreadRequirementEnabled = false
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Preconditions.mainThreadRequirementEnabled = true
        Dispatchers.resetMain()
    }

    @Test
    fun `should schedule and run action`() {
        var actionTriggered = false
        actionsScheduler.schedule("tag1") {
            actionTriggered = true
        }
        testDispatcher.scheduler.runCurrent()
        assertTrue(actionTriggered)
        assertFalse(actionsScheduler.hasPendingActions())
    }

    @Test
    fun `has pending actions when action scheduled`() {
        actionsScheduler.schedule("tag1") {}
        assertTrue(actionsScheduler.hasPendingActions())
    }

    @Test
    fun `no pending actions when action is running`() {
        actionsScheduler.schedule("tag1") {
            delay(100)
        }
        testDispatcher.scheduler.runCurrent()
        assertFalse(actionsScheduler.hasPendingActions())
    }

    @Test
    fun `should cancel command by tag`() {
        actionsScheduler.schedule("tag1") { }
        actionsScheduler.schedule("tag2") {}

        actionsScheduler.cancel("tag1")
        actionsScheduler.cancel("tag2")

        testDispatcher.scheduler.runCurrent()

        assertFalse(actionsScheduler.hasPendingActions())
    }

    @Test
    fun `should cancel all commands`() {
        actionsScheduler.schedule("tag1") {}
        actionsScheduler.schedule("tag2") {}

        actionsScheduler.cancelAll()

        testDispatcher.scheduler.runCurrent()

        assertFalse(actionsScheduler.hasPendingActions())
    }

}