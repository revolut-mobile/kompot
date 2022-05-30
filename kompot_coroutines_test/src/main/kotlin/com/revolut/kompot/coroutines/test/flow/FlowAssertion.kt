package com.revolut.kompot.coroutines.test.flow

interface FlowAssertion<T> {

    val values: List<T>

    val valueCount: Int

    fun assertValueCount(
        expectedCount: Int
    ): FlowAssertion<T>

    fun assertValues(
        vararg expectedValues: T
    ): FlowAssertion<T>

    fun assertValues(
        expectedValues: List<T>
    ): FlowAssertion<T>

    fun assertValueAt(
        index: Int, expectedValue: T
    ): FlowAssertion<T>

    fun assertValueAt(
        index: Int,
        predicate: (actualValue: T) -> Boolean
    ): FlowAssertion<T>

    fun assertNoValues(): FlowAssertion<T>

    fun assertComplete(): FlowAssertion<T>

    fun assertError(
        throwable: Throwable
    ): FlowAssertion<T>

    fun <R : Throwable> assertError(
        throwableClass: Class<R>
    ): FlowAssertion<T>
}