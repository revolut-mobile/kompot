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

package com.revolut.kompot.cache

import android.view.LayoutInflater
import android.view.View
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.BaseFlowComponent
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerExtension
import com.revolut.kompot.navigable.ControllerKey
import com.revolut.kompot.navigable.cache.ControllerCacheStrategy
import com.revolut.kompot.navigable.cache.ControllersCache
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.flow.BaseFlow
import com.revolut.kompot.navigable.flow.BaseFlowModel
import com.revolut.kompot.navigable.flow.FlowState
import com.revolut.kompot.navigable.flow.FlowStateWrapper
import com.revolut.kompot.navigable.flow.FlowStep
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class DefaultControllersCacheTest {

    @Test
    fun `should cache controller`() {
        val cache = DefaultControllersCache(1)
        cache.onControllerCreated(createController("testKey", cache))

        assertTrue(cache.isControllerCached(ControllerKey("testKey")))
    }

    @Test
    fun `should remove controller from cache if no space`() {
        val cache = DefaultControllersCache(1)
        cache.onControllerCreated(createController("testKey1", cache))
        cache.onControllerCreated(createController("testKey2", cache))

        assertTrue(cache.isControllerCached(ControllerKey("testKey2")))
        assertFalse(cache.isControllerCached(ControllerKey("testKey1")))
    }

    @Test
    fun `should not remove newly added controller`() {
        val cache = DefaultControllersCache(0)
        cache.onControllerCreated(createController("testKey", cache))

        assertTrue(cache.isControllerCached(ControllerKey("testKey")))
    }

    @Test
    fun `should not remove strongly stored controller`() {
        val cache = DefaultControllersCache(1)
        cache.onControllerCreated(createController("testKey1", cache, cacheStrategy = ControllerCacheStrategy.Prioritized))
        cache.onControllerCreated(createController("testKey2", cache))

        assertTrue(cache.isControllerCached(ControllerKey("testKey2")))
        assertTrue(cache.isControllerCached(ControllerKey("testKey1")))
    }

    @Test
    fun `weak stored flow removed from cache with dependent child`() {

        val cache = DefaultControllersCache(2)

        val flow = createFlow("testFlowKey", cache, children = listOf("testChildKey"))
        val child = createController("testChildKey", cache, parent = flow)

        cache.onControllerCreated(flow)
        cache.onControllerCreated(child)
        cache.onControllerCreated(createController("guy she told not worry about", cache))

        assertFalse(cache.isControllerCached(ControllerKey("testFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("testChildKey")))

        assertTrue(cache.isControllerCached(ControllerKey("guy she told not worry about")))
    }

    @Test
    fun `weak stored flow not removed from cache if dependent child was force removed`() {

        val cache = DefaultControllersCache(2)

        val flow = createFlow("testFlowKey", cache, children = listOf("testChildKey"))
        val child = createController("testChildKey", cache, parent = flow)

        cache.onControllerCreated(flow)
        cache.onControllerCreated(child)
        cache.removeController(child.key, finish = true)

        assertTrue(cache.isControllerCached(ControllerKey("testFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("testChildKey")))
    }

    @Test
    fun `remove nested flow if its last child removed from cache`() {

        val cache = DefaultControllersCache(2)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertFalse(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    @Test
    fun `do not remove nested flow if one its children has prioritized strategy`() {

        val cache = DefaultControllersCache(2)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow, cacheStrategy = ControllerCacheStrategy.Prioritized)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    @Test
    fun `remove nested flow with GRAND child from cache`() {

        val cache = DefaultControllersCache(2)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedNestedFlowKey", "nestedChildKey2", "nestedChildKey3"), parent = mainFlow)

        val nestedNestedFlow = createFlow("nestedNestedFlowKey", cache, children = listOf("nestedNestedChildKey"), parent = nestedFlow)
        val nestedNestedFlowChild = createController("nestedNestedChildKey", cache, parent = nestedNestedFlow)

        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)
        val nestedFlowChild3 = createController("nestedChildKey3", cache, parent = nestedFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedNestedFlow)
            onControllerCreated(nestedNestedFlowChild)
            onControllerCreated(nestedFlowChild2)
            onControllerCreated(nestedFlowChild3)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedNestedFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedNestedChildKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey2")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey3")))
    }

    @Test
    fun `do not remove nested flow with GRAND child from cache if GRAND child is prioritized`() {

        val cache = DefaultControllersCache(2)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedNestedFlowKey", "nestedChildKey2", "nestedChildKey3"), parent = mainFlow)

        val nestedNestedFlow = createFlow("nestedNestedFlowKey", cache, children = listOf("nestedNestedChildKey"), parent = nestedFlow)
        val nestedNestedFlowChild = createController("nestedNestedChildKey", cache, parent = nestedNestedFlow, cacheStrategy = ControllerCacheStrategy.Prioritized)

        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)
        val nestedFlowChild3 = createController("nestedChildKey3", cache, parent = nestedFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedNestedFlow)
            onControllerCreated(nestedNestedFlowChild)
            onControllerCreated(nestedFlowChild2)
            onControllerCreated(nestedFlowChild3)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedNestedFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedNestedChildKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey2")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey3")))
    }

    @Test
    fun `cache simple nested hierarchy with threshold=3`() {

        val cache = DefaultControllersCache(3)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertFalse(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    @Test
    fun `cache simple nested hierarchy with threshold=4`() {

        val cache = DefaultControllersCache(4)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    @Test
    fun `cache simple nested hierarchy with threshold=5`() {

        val cache = DefaultControllersCache(5)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertFalse(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    @Test
    fun `cache simple nested hierarchy with threshold=6`() {

        val cache = DefaultControllersCache(6)

        val mainFlow = createFlow("mainFlowKey", cache, children = listOf("mainChildKey1", "nestedFlowKey", "mainChildKey2"))
        val mainFlowChild = createController("mainChildKey1", cache, parent = mainFlow)

        val nestedFlow = createFlow("nestedFlowKey", cache, children = listOf("nestedChildKey1", "nestedChildKey2"), parent = mainFlow)
        val nestedFlowChild1 = createController("nestedChildKey1", cache, parent = nestedFlow)
        val nestedFlowChild2 = createController("nestedChildKey2", cache, parent = nestedFlow)

        val mainFlowChild2 = createController("mainChildKey2", cache, parent = mainFlow)

        with(cache) {
            onControllerCreated(mainFlow)
            onControllerCreated(mainFlowChild)

            onControllerCreated(nestedFlow)
            onControllerCreated(nestedFlowChild1)
            onControllerCreated(nestedFlowChild2)

            onControllerCreated(mainFlowChild2)
        }

        assertTrue(cache.isControllerCached(ControllerKey("mainFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey1")))

        assertTrue(cache.isControllerCached(ControllerKey("nestedFlowKey")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey1")))
        assertTrue(cache.isControllerCached(ControllerKey("nestedChildKey2")))

        assertTrue(cache.isControllerCached(ControllerKey("mainChildKey2")))
    }

    private fun createController(
        key: String,
        cache: ControllersCache,
        cacheStrategy: ControllerCacheStrategy = ControllerCacheStrategy.Auto,
        parent: Controller? = null
    ): Controller {
        return TestController(key, cache, cacheStrategy).apply {
            this.parentController = parent
        }
    }

    private fun createFlow(
        key: String,
        cache: ControllersCache,
        children: List<String>,
        parent: Controller? = null
    ): BaseFlow<TestStep, IOData.EmptyInput, IOData.EmptyOutput> {
        return TestFlow(key, cache, children).apply {
            this.parentController = parent
        }
    }

    class TestController(
        key: String,
        cache: ControllersCache,
        override var cacheStrategy: ControllerCacheStrategy
    ) : Controller() {

        override var keyInitialization: () -> ControllerKey = {
            ControllerKey(key)
        }
        override val layoutId = 1

        init {
            parentControllerManager = mock {
                on { controllersCache } doReturn cache
            }
        }

        override fun createView(inflater: LayoutInflater): View {
            throw IllegalStateException()
        }
    }

    class TestFlow(
        key: String,
        cache: ControllersCache,
        private val children: List<String>
    ) : BaseFlow<TestStep, IOData.EmptyInput, IOData.EmptyOutput>(IOData.EmptyInput) {

        override var keyInitialization: () -> ControllerKey = {
            ControllerKey(key)
        }
        override val component: BaseFlowComponent
            get() = throw IllegalStateException()
        override val controllerDelegates: Set<ControllerExtension>
            get() = emptySet()

        private val stateWrapper = createSampleStateWrapper()
        private val backStack = createSampleBackStack()

        override val flowModel = mock<BaseFlowModel<State, TestStep, IOData.EmptyOutput>> {
            on { stateWrapper } doReturn stateWrapper
            on { backStack } doReturn backStack
        }

        init {
            parentControllerManager = mock {
                on { controllersCache } doReturn cache
            }
        }

        override fun updateUi(step: TestStep) = Unit

        private fun createSampleStateWrapper() = FlowStateWrapper(
            state = State,
            step = TestStep,
            currentControllerKey = ControllerKey(children.last())
        )

        private fun createSampleBackStack() = LinkedList<FlowStateWrapper<State, TestStep>>().apply {
            if (children.isEmpty()) {
                add(
                    FlowStateWrapper(
                        state = State,
                        step = TestStep,
                        currentControllerKey = null
                    )
                )
                return@apply
            }
            children.asReversed().drop(1).forEach { key ->
                add(
                    FlowStateWrapper(
                        state = State,
                        step = TestStep,
                        currentControllerKey = ControllerKey(key)
                    )
                )
            }
        }

    }

    @Parcelize
    object TestStep : FlowStep

    @Parcelize
    object State : FlowState

}