package com.revolut.kompot.navigable

import com.revolut.kompot.navigable.utils.Preconditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationQueueTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Preconditions.mainThreadRequirementEnabled = false
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Preconditions.mainThreadRequirementEnabled = true
        Dispatchers.resetMain()
    }

    @Test
    fun `should throw exception if flow starts multiple commands in parallel`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)
        assertThrows<IllegalStateException> {
            flowModel.back()
        }
    }

    @Test
    fun `should navigate to steps sequentially`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)
        testDispatcher.scheduler.runCurrent()
        flowModel.next(TestStep.Step1, true)

        assertEquals(TestStep.Step1, flowModel.step)
    }

    @Test
    fun `flow should cancel navigation commands when destroyed`() {
        val flowModel = TestFlowModel()
        val flow = TestFlow(flowModel)

        flow.onCreate()
        flow.onAttach()
        flowModel.next(TestStep.Step2, true)
        flow.onDestroy()

        testDispatcher.scheduler.runCurrent()

        assertFalse(flow.findRootFlow().navActionsScheduler.hasPendingActions())
    }

}