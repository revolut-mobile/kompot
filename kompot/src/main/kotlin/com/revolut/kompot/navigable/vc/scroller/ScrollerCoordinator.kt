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

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.revolut.kompot.common.IOData
import com.revolut.kompot.navigable.Controller
import com.revolut.kompot.navigable.ControllerModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

open class ScrollerCoordinator<S : ScrollerItem>(
    initialItems: ScrollerItems<S>,
    private val controllersFactory: ScrollerCoordinator<S>.(S) -> Controller,
) {

    private val _itemUpdates = MutableStateFlow(
        ScrollerItemsUpdate(
            items = initialItems.items,
            selectedItemId = initialItems.selectedItemId,
            smoothScroll = false,
        )
    )
    private val _scrollerCommands = MutableSharedFlow<ScrollerCommand>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val latestItemUpdate get() = _itemUpdates.value

    fun itemUpdatesStream(): Flow<ScrollerItemsUpdate<S>> = _itemUpdates

    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    fun getController(scrollerItem: S): Controller = controllersFactory(scrollerItem)

    internal fun scrollerCommandsStream(): Flow<ScrollerCommand> = _scrollerCommands

    internal fun updateItems(
        selectedItemId: String? = null,
        items: List<S> = latestItemUpdate.items,
        smoothScroll: Boolean = true,
    ) {
        _itemUpdates.tryEmit(
            ScrollerItemsUpdate(
                items = items,
                selectedItemId = selectedItemId,
                smoothScroll = smoothScroll
            )
        )
    }

    internal fun quit() {
        _scrollerCommands.tryEmit(ScrollerCommand.Quit)
    }
}

@Suppress("FunctionName")
fun <S : ScrollerItem, Out : IOData.Output, T> T.ScrollerCoordinator(
    initialItems: ScrollerItems<S>,
    controllersFactory: ScrollerCoordinator<S>.(S) -> Controller,
): ScrollerCoordinator<S> where T : ScrollerViewModel<S, Out>,
                                 T : ControllerModel =
    com.revolut.kompot.navigable.vc.scroller.ScrollerCoordinator(
        initialItems = initialItems,
        controllersFactory = controllersFactory,
    )

sealed interface ScrollerCommand {
    object Quit : ScrollerCommand
}