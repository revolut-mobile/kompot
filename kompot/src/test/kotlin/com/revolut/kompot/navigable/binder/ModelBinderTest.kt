package com.revolut.kompot.navigable.binder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModelBinderTest {

    private val binder = DefaultModelBinder<Int>()

    @Test
    fun `should notify observer about emission`() {
        var emissionsCount = 0
        binder.bind { emissionsCount++ }

        binder.notify(1)

        assertEquals(1, emissionsCount)
    }

    @Test
    fun `should notify multiple observers about emission`() {
        var firstObserverEmissionsCount = 0
        var secondObserverEmissionsCount = 0
        binder.bind { firstObserverEmissionsCount++ }
        binder.bind { secondObserverEmissionsCount++ }

        binder.notify(1)

        assertEquals(1, firstObserverEmissionsCount)
        assertEquals(1, secondObserverEmissionsCount)
    }

    @Test
    fun `should notify observers with correctEmissions`() {
        val binder = ModelBinder<String>()

        val firstObserverEmissions = mutableListOf<String>()
        val secondObserverEmissions = mutableListOf<String>()
        binder.bind { firstObserverEmissions.add(it) }
        binder.bind { secondObserverEmissions.add(it) }

        binder.notify("1")
        binder.notify("2")
        binder.notify("3")

        val expectedEmissions = listOf("1", "2", "3")

        assertEquals(expectedEmissions, firstObserverEmissions)
        assertEquals(expectedEmissions, secondObserverEmissions)
    }

    @Test
    fun `should remove reference on removed observer`() {
        val observer = ModelObserver<Int> {}

        binder.bind(observer)
        binder.unbind(observer)

        assertTrue(binder.observers.isEmpty())
    }

    @Test
    fun `should not notify a removed observer`() {
        var emissionsCount = 0
        val observer = ModelObserver<Int> {
            emissionsCount ++
        }

        binder.bind(observer)
        binder.unbind(observer)

        assertEquals(0, emissionsCount)
    }

}