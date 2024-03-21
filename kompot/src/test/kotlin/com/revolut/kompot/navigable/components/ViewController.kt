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

package com.revolut.kompot.navigable.components

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.ControllerManager
import com.revolut.kompot.navigable.cache.DefaultControllersCache
import com.revolut.kompot.navigable.root.NavActionsScheduler
import com.revolut.kompot.navigable.root.RootFlow
import com.revolut.kompot.navigable.vc.SimpleViewController
import com.revolut.kompot.navigable.vc.SimpleViewControllerModel
import com.revolut.kompot.view.ControllerContainerFrameLayout
import kotlinx.parcelize.Parcelize

internal class TestViewController(
    val input: String,
    override val themeId: Int? = null,
    override val viewSavedStateEnabled: Boolean = true,
    val viewMarker: Int = 1,
    val instrumented: Boolean = true,
) : SimpleViewController<IOData.EmptyOutput>() {

    val model = SimpleViewControllerModel<IOData.EmptyOutput>()
    override val controllerModel: SimpleViewControllerModel<IOData.EmptyOutput> = model
    override val layoutId: Int = 0

    init {
        val parentControllerManager: ControllerManager = mock {
            on { controllersCache } doReturn DefaultControllersCache(20)
        }
        val rootFlow: RootFlow<*, *> = mock {
            on { rootDialogDisplayer } doReturn mock()
            on { navActionsScheduler } doReturn NavActionsScheduler()
        }
        bind(parentControllerManager, parentController = rootFlow)

        view = if (instrumented) {
            TestControllerView(TestControllerActivity(), viewMarker).apply {
                id = 11
            }
        } else {
            val mockedActivity = mock<Activity> {
                on { window } doReturn mock()
            }
            mock {
                on { context } doReturn mockedActivity
            }
        }
    }

    override fun createView(inflater: LayoutInflater): View = view
}

internal class TestControllerActivity : Activity() {
    init {
        attachBaseContext(ApplicationProvider.getApplicationContext())
    }
}

class TestControllerView(context: Context, val marker: Int) : ControllerContainerFrameLayout(context) {

    var restoredMarker: Int? = null

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(
            marker = marker,
            baseState = superState,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            restoredMarker = state.marker
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    @Parcelize
    data class SavedState(
        val marker: Int,
        val baseState: Parcelable?,
    ) : BaseSavedState(baseState)
}