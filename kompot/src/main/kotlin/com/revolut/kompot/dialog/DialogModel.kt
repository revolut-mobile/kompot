package com.revolut.kompot.dialog

interface DialogModel<Result : DialogModelResult>

interface DialogModelResult

object EmptyDialogModelResult : DialogModelResult