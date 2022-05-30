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
    fun `throw an exception with unhandled delegate model`() {
        val firstDelegate = spy(FirstFakeDialogDisplayerDelegate())
        val secondDelegate = spy(SecondFakeDialogDisplayerDelegate())
        val displayer = DialogDisplayer(FakeLoadingDialogDisplayer, listOf(firstDelegate, secondDelegate))

        assertThrows(IllegalStateException::class.java, { displayer.showDialog(UnknownDialogModel("")) })
    }

    private data class UnknownDialogModel(val message: String) : DialogModel<EmptyDialogModelResult>
}