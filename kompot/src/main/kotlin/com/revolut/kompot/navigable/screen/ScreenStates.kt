package com.revolut.kompot.navigable.screen

import android.os.Parcelable
import com.revolut.recyclerkit.delegates.ListItem
import java.io.Serializable

interface ScreenStates {
    interface Domain

    interface RetainedDomain: Parcelable

    interface UI {
        fun calculatePayload(oldState: UI): UIPayload? = null
    }

    interface UIPayload

    interface UIList : UI {
        val items: List<ListItem>
    }

    object EmptyDomain : Domain

    object EmptyUI : UI

    object EmptyUIList : UIList {
        override val items: List<ListItem> = emptyList()
    }
}

data class DomainStateProperty<T, E : DomainStatePropertyError>(val value: T, val error: E? = null, val loading: Boolean = false) : Serializable

fun <T> T.asDomainStateProperty() = DomainStateProperty<T, DomainStatePropertyError>(value = this, error = null, loading = true)

val <T : Any> DomainStateProperty<List<T>, *>.firstLoading get() = loading && value.isEmpty()