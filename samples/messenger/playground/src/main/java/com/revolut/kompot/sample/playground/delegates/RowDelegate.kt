package com.revolut.kompot.sample.playground.delegates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.revolut.kompot.sample.playground.R
import com.revolut.recyclerkit.delegates.BaseRecyclerViewDelegate
import com.revolut.recyclerkit.delegates.BaseRecyclerViewHolder
import com.revolut.recyclerkit.delegates.ListItem

class RowDelegate : BaseRecyclerViewDelegate<RowDelegate.Model, RowDelegate.ViewHolder>(
    R.layout.delegate_row,
    { _, data -> data is Model }
) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.delegate_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, data: Model, pos: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, data, pos, payloads)

        val currentPayloads = payloads?.mapNotNull { it as? Payload }
        if (currentPayloads.isNullOrEmpty()) {
            holder.title.text = data.title
            holder.subtitle.text = data.subtitle
        } else {
            currentPayloads.forEach { payload ->
                if (payload.titleChanged) {
                    holder.title.text = data.title
                }
                if (payload.subtitleChanged) {
                    holder.subtitle.text = data.subtitle
                }
            }
        }
    }

    class ViewHolder(itemView: View) : BaseRecyclerViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
    }

    data class Model(
        override val listId: String,
        val title: String?,
        val subtitle: String?
    ) : ListItem {
        override fun calculatePayload(oldItem: Any): Any? {
            return (oldItem as? Model)?.let {
                Payload(
                    titleChanged = oldItem.title != title,
                    subtitleChanged = oldItem.subtitle != subtitle
                )
            }
        }
    }

    data class Payload(
        val titleChanged: Boolean,
        val subtitleChanged: Boolean
    )
}