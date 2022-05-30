package com.revolut.kompot.navigable.root

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.revolut.kompot.common.IOData
import com.revolut.kompot.common.ModalDestination
import com.revolut.kompot.common.NavigationDestination
import com.revolut.kompot.common.NavigationEvent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Test

internal class BaseRootFlowModelTest {

    @Parcelize
    class TestState : FlowState

    @Parcelize
    class TestStep : FlowStep

    val testNavigationDestination: NavigationDestination = ModalDestination.ExplicitScreen<IOData.EmptyOutput>(mock())
    val testNavigationEvent = NavigationEvent(testNavigationDestination)

    @Test
    fun `GIVEN model has overriden destination handling WHEN handle overriden destination THEN super handling is not called`() {
        val model = object : BaseRootFlowModel<TestState, TestStep>() {
            override val initialStep: TestStep = mock()
            override val initialState: TestState = mock()
            override fun getController(step: TestStep): Controller = mock()

            override fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean {
                return navigationDestination == testNavigationEvent.destination
            }
        }
        val rootNavigator: RootNavigator = mock()
        model.rootNavigator = rootNavigator

        model.tryHandleEvent(testNavigationEvent)

        verify(rootNavigator, never()).openModal(any(), any())
    }

    @Test
    fun `GIVEN model has overriden destination handling WHEN handle regular destination THEN super handling is called`() {
        val model = object : BaseRootFlowModel<TestState, TestStep>() {
            override val initialStep: TestStep = mock()
            override val initialState: TestState = mock()
            override fun getController(step: TestStep): Controller = mock()

            override fun handleNavigationDestination(navigationDestination: NavigationDestination): Boolean {
                return navigationDestination == testNavigationEvent.destination
            }
        }
        val rootNavigator: RootNavigator = mock()
        model.rootNavigator = rootNavigator

        val event = NavigationEvent(ModalDestination.ExplicitScreen<IOData.EmptyOutput>(mock()))
        event._controller = mock()
        model.tryHandleEvent(event)

        verify(rootNavigator).openModal(any(), any())
    }
}