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

package com.revolut.kompot.view

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
internal class ControllerContainerTest {

    @Test
    fun `GIVEN no running transition WHEN dispatch touch event THEN don't intercept event`() {
        getControllerContainers(ApplicationProvider.getApplicationContext()).forEach { container ->
            assertFalse(container.dispatchTouchEvent(createMotionEvent()))
        }
    }

    @Test
    fun `GIVEN running definite transition WHEN dispatch touch event THEN intercept event`() {
        getControllerContainers(ApplicationProvider.getApplicationContext()).forEach { container ->
            require(container is ControllerContainer)
            container.onControllersTransitionStart(indefinite = false)
            assertTrue(container.dispatchTouchEvent(createMotionEvent()))
        }
    }

    @Test
    fun `GIVEN running indefinite transition WHEN dispatch touch event THEN don't intercept event`() {
        getControllerContainers(ApplicationProvider.getApplicationContext()).forEach { container ->
            require(container is ControllerContainer)
            container.onControllersTransitionStart(indefinite = true)
            assertFalse(container.dispatchTouchEvent(createMotionEvent()))
        }
    }

    @Test
    fun `GIVEN ended transition WHEN dispatch touch event THEN don't intercept event`() {
        getControllerContainers(ApplicationProvider.getApplicationContext()).forEach { container ->
            require(container is ControllerContainer)
            container.onControllersTransitionStart(indefinite = false)
            container.onControllersTransitionEnd(indefinite = false)
            assertFalse(container.dispatchTouchEvent(createMotionEvent()))
        }
    }

    @Test
    fun `GIVEN canceled transition WHEN dispatch touch event THEN don't intercept event`() {
        getControllerContainers(ApplicationProvider.getApplicationContext()).forEach { container ->
            require(container is ControllerContainer)
            container.onControllersTransitionStart(indefinite = false)
            container.onControllersTransitionCanceled(indefinite = false)
            assertFalse(container.dispatchTouchEvent(createMotionEvent()))
        }
    }

    private fun getControllerContainers(context: Context): List<ViewGroup> = listOf(
        ControllerContainerFrameLayout(context),
        ControllerContainerLinearLayout(context),
        ControllerContainerConstraintLayout(context),
    )

    private fun createMotionEvent() = MotionEvent.obtain(
        SystemClock.uptimeMillis(),
        SystemClock.uptimeMillis(),
        MotionEvent.ACTION_UP,
        0f,
        0f,
        0
    )
}