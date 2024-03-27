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

package com.revolut.kompot.navigable.vc.scroller

import android.os.Parcelable
import com.revolut.kompot.common.IOData
import com.revolut.kompot.di.flow.ParentFlow
import com.revolut.kompot.navigable.vc.ViewControllerApi
import com.revolut.kompot.navigable.vc.binding.ModelBinding
import com.revolut.kompot.navigable.vc.binding.ViewControllerModelApi

interface ScrollerModelBinding<S : ScrollerItem> : ModelBinding

interface ScrollerViewController<S : ScrollerItem> : ViewControllerApi, ParentFlow {
    override val modelBinding: ScrollerModelBinding<S>
    override val hasBackStack: Boolean get() = false

    fun onCompletelyVisibleItemChanged(scrollerItem: S) = Unit
}

interface ScrollerViewModel<S : ScrollerItem, Out : IOData.Output> : ViewControllerModelApi<Out> {
    val scrollerCoordinator: ScrollerCoordinator<S>
}

data class ScrollerItems<S : ScrollerItem>(
    val items: List<S>,
    val selectedItemId: String = requireNotNull(items.firstOrNull()?.id) { "Non empty list should be provided for items" }
) {

    companion object {
        operator fun <S : ScrollerItem> invoke(vararg items: S): ScrollerItems<S> = ScrollerItems(items = items.toList())
    }
}

data class ScrollerItemsUpdate<S : ScrollerItem>(
    val items: List<S>,
    val selectedItemId: String?,
    val smoothScroll: Boolean,
)

interface ScrollerItem : Parcelable {
    val id: String
}

interface FixedIdScrollerItem : ScrollerItem {

    override val id: String get() = this.javaClass.name
}

enum class ScrollMode {
    HORIZONTAL, VERTICAL, PAGER
}