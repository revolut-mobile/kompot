package com.revolut.kompot.dialog

import com.revolut.kompot.dispatchBlockingTest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DialogDisplayerDelegateTest {
    private val dialogDisplayer = FirstFakeDialogDisplayerDelegate()

    @Test
    fun `test postResult`() = dispatchBlockingTest {
        val results = mutableListOf<FirstFakeDialogModelResult>()

        launch {
            dialogDisplayer.showDialog<FirstFakeDialogModelResult>(
                dialogModel = FirstFakeDialogModel("")
            ).take(3).toList(results)
        }

        assertTrue(results.isEmpty())

        dialogDisplayer.testPostResult()
        assertEquals(1, results.size)

        dialogDisplayer.testPostResult()
        dialogDisplayer.testPostResult()
        assertEquals(3, results.size)
    }

    @Test
    fun `test that postResult does not affect previous subscription`() = dispatchBlockingTest {
        val firstDialogResults = mutableListOf<FirstFakeDialogModelResult>()

        launch {
            dialogDisplayer.showDialog<FirstFakeDialogModelResult>(
                dialogModel = FirstFakeDialogModel("")
            ).toList(firstDialogResults)
        }

        assertTrue(firstDialogResults.isEmpty())

        dialogDisplayer.testPostResult()
        assertEquals(1, firstDialogResults.size)

        val secondDialogResults = mutableListOf<FirstFakeDialogModelResult>()

        launch {
            dialogDisplayer.showDialog<FirstFakeDialogModelResult>(
                dialogModel = FirstFakeDialogModel("")
            ).take(1).toList(secondDialogResults)
        }

        assertTrue(secondDialogResults.isEmpty())

        dialogDisplayer.testPostResult()
        assertEquals(1, secondDialogResults.size)
    }
}