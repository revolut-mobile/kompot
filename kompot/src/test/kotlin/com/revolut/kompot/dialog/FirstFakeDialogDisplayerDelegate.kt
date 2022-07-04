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

internal class FirstFakeDialogDisplayerDelegate : DialogDisplayerDelegate<FirstFakeDialogModel>() {
    override fun canHandle(dialogModel: DialogModel<*>): Boolean = dialogModel is FirstFakeDialogModel

    override fun showDialogInternal(dialogModel: FirstFakeDialogModel) {
        //do nothing
    }

    override fun hideDialog() {
        //do nothing
    }

    fun testPostResult() = postResult(FirstFakeDialogModelResult)
}

data class FirstFakeDialogModel(val message: String) : DialogModel<FirstFakeDialogModelResult>

object FirstFakeDialogModelResult : DialogModelResult