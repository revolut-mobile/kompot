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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class DialogDisplayerTest {
    @Test
    fun `test that all delegates methods were called`() {
        val delegate = mock<DialogDisplayerDelegate<*>>()
        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(delegate))

        displayer.onAttach()
        displayer.onDetach()
        displayer.onCreate()
        displayer.onDestroy()

        verify(delegate).onAttach()
        verify(delegate).onDetach()
        verify(delegate).onCreate()
        verify(delegate).onDestroy()
    }

    @Test
    fun `test proper delegate handling`() {
        val firstDelegate = spy(FirstFakeDialogDisplayerDelegate())
        val secondDelegate = spy(SecondFakeDialogDisplayerDelegate())
        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(firstDelegate, secondDelegate))

        val dialogModel = FirstFakeDialogModel("")
        displayer.showDialog(dialogModel)
        displayer.hideAllDialogs()

        verify(firstDelegate).showDialog<FirstFakeDialogModelResult>(any<FirstFakeDialogModel>())
        verify(secondDelegate, never()).showDialog<SecondFakeDialogModelResult>(any<SecondFakeDialogModel>())

        verify(firstDelegate).hideDialog()
        verify(secondDelegate).hideDialog()
    }

    @Test
    fun `GIVEN dialog model WHEN hide dialog THEN proxy call to correct delegate`() {
        val firstDelegate = spy(FirstFakeDialogDisplayerDelegate())
        val dialogModel = FirstFakeDialogModel("")

        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(firstDelegate))

        displayer.hideDialog(dialogModel)

        verify(firstDelegate).hideDialog()
    }

    @Test
    fun `throw an exception with unhandled delegate model`() {
        val firstDelegate = spy(FirstFakeDialogDisplayerDelegate())
        val secondDelegate = spy(SecondFakeDialogDisplayerDelegate())
        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(firstDelegate, secondDelegate))

        assertThrows(IllegalStateException::class.java, { displayer.showDialog(UnknownDialogModel("")) })
    }

    @Test
    fun `GIVEN unknown dialog model WHEN hide dialog THEN throw an exception`() {
        val firstDelegate = spy(FirstFakeDialogDisplayerDelegate())
        val secondDelegate = spy(SecondFakeDialogDisplayerDelegate())
        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(firstDelegate, secondDelegate))

        assertThrows(IllegalStateException::class.java) { displayer.hideDialog(UnknownDialogModel("")) }
    }

    private data class UnknownDialogModel(val message: String) : DialogModel<EmptyDialogModelResult>
}