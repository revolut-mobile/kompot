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