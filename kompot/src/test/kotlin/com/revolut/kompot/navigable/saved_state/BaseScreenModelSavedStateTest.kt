package com.revolut.kompot.navigable.saved_state

import com.revolut.kompot.common.IOData
import com.revolut.kompot.dispatchBlockingTest
import com.revolut.kompot.navigable.screen.BaseScreenModel
import com.revolut.kompot.navigable.screen.ScreenStates
import com.revolut.kompot.navigable.screen.StateMapper
import com.revolut.kompot.navigable.screen.state.SaveStateDelegate
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class BaseScreenModelSavedStateTest {

    @Test
    fun `should save and restore domain state`() = dispatchBlockingTest {
        val screenModel = TestBaseScreenModel()
        screenModel.onCreated()

        screenModel.pushState(
            DomainState(
                retainedValue = 2,
                nonRetainedValue = "2"
            )
        )

        val bundle = screenModel.saveState()

        val restoredScreenModel = TestBaseScreenModel()

        restoredScreenModel.restoreState(bundle)

        val actualStates = mutableListOf<DomainState>()
        launch {
            restoredScreenModel.domainStateStream().toCollection(actualStates)
        }

        restoredScreenModel.onCreated()

        val expected = listOf(
            DomainState(
                retainedValue = 2,
                nonRetainedValue = "1"
            )
        )

        assertEquals(expected, actualStates)
    }

}

private data class DomainState(
    val retainedValue: Int,
    val nonRetainedValue: String,
) : ScreenStates.Domain

@Parcelize
private data class RetainedState(
    val value: Int,
): ScreenStates.RetainedDomain

private class TestBaseScreenModel : BaseScreenModel<DomainState, TestUIState, IOData.EmptyOutput>(TestStateMapper()) {

    override val initialState: DomainState = DomainState(
        retainedValue = 1,
        nonRetainedValue = "1"
    )

    override val saveStateDelegate = object : SaveStateDelegate<DomainState, RetainedState>() {

        override fun getRetainedState(currentState: DomainState) = RetainedState(value = currentState.retainedValue)

        override fun restoreDomainState(
            initialState: DomainState,
            retainedState: RetainedState
        ) = DomainState(
            retainedValue = retainedState.value,
            nonRetainedValue = initialState.nonRetainedValue
        )

    }

    fun pushState(domainState: DomainState) = updateState { domainState }
}

private class TestUIState : ScreenStates.UI

private class TestStateMapper : StateMapper<DomainState, TestUIState> {
    override fun mapState(domainState: DomainState): TestUIState = TestUIState()
}