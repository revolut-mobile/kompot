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

package com.revolut.kompot.common

import android.os.Parcelable
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize

interface IOData {
    interface Input : Parcelable

    interface Output

    @Parcelize
    object EmptyInput : Input

    object EmptyOutput : Output

    companion object {
        const val INPUT_BUNDLE_ARG = "INPUT_BUNDLE_ARG"
    }
}

fun IOData.Input.toBundle() = bundleOf(IOData.INPUT_BUNDLE_ARG to this)