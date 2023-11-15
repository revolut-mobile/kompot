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

package com.revolut.kompot.navigable.vc

import android.os.Bundle
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi
import com.revolut.kompot.navigable.vc.di.EmptyViewControllerComponent
import com.revolut.kompot.navigable.vc.di.ViewControllerComponent

abstract class SimpleViewController<Output : IOData.Output> : ViewController<Output>() {
    override val controllerModel: ViewControllerModelApi<Output> = SimpleViewControllerModel()
    override val modelBinding: ModelBinding = SimpleModelBinding()
    override val component: ViewControllerComponent = EmptyViewControllerComponent
}

internal class SimpleViewControllerModel<Output : IOData.Output> : ViewControllerModel<Output>()

internal class SimpleModelBinding : ModelBinding {
    override fun saveState(outState: Bundle) = Unit
    override fun restoreState(state: Bundle) = Unit
}