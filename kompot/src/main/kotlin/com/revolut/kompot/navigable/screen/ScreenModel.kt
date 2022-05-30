package com.revolut.kompot.navigable.screen

import android.os.Bundle
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.binder.ModelBinder
import kotlinx.coroutines.flow.Flow

interface ScreenModel<UI_STATE : ScreenStates.UI, OUTPUT : IOData.Output> {

    fun uiStateStream(): Flow<UI_STATE>

    fun resultsBinder(): ModelBinder<OUTPUT>

    fun backPressBinder(): ModelBinder<Unit>

    fun saveState(): Bundle

    fun restoreState(state: Bundle)
}