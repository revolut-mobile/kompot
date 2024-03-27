package com.revolut.kompot.sample.ui_common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.revolut.recyclerkit.delegates.BaseRecyclerViewDelegate
import com.revolut.recyclerkit.delegates.BaseRecyclerViewHolder
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class RowDelegate : BaseRecyclerViewDelegate<RowDelegate.Model, RowDelegate.ViewHolder>(
    R.layout.view_delegate_row,
    { _, data -> data is Model }
) {

    private val onItemClickSharedFlow by lazy(LazyThreadSafetyMode.NONE) {
        MutableSharedFlow<Model>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_delegate_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, data: Model, pos: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, data, pos, payloads)

        holder.itemView.setOnClickListener { onItemClickSharedFlow.tryEmit(data) }

        if (payloads.isEmpty()) {
            holder.image.setImageResource(data.image)
            holder.title.setTextOrHide(data.title)
            holder.subtitle.setTextOrHide(data.subtitle)
            holder.caption.setTextOrHide(data.caption)
            holder.badge.setTextOrHide(data.badge)
        } else {
            payloads.filterIsInstance(Payload::class.java).forEach { payload ->
                if (payload.imageChanged) {
                    holder.image.setImageResource(data.image)
                }
                if (payload.titleChanged) {
                    holder.title.setTextOrHide(data.title)
                }
                if (payload.subtitleChanged) {
                    holder.subtitle.setTextOrHide(data.subtitle)
                }
                if (payload.captionChanged) {
                    holder.caption.setTextOrHide(data.caption)
                }
                if (payload.badgeChanged) {
                    holder.badge.setTextOrHide(data.badge)
                }
            }
        }

    }

    private fun TextView.setTextOrHide(content: String?) {
        if (content == null) {
            text = ""
            visibility = View.GONE
        } else {
            text = content
            visibility = View.VISIBLE
        }
    }

    private fun TextView.setTextOrHide(textModel: TextModel?) {
        setTextOrHide(textModel?.content)
        val color = textModel?.color ?: return
        setTextColor(ContextCompat.getColor(context, color))
    }

    class ViewHolder(itemView: View) : BaseRecyclerViewHolder(itemView) {
        val image: ImageView by lazy(LazyThreadSafetyMode.NONE) {
            itemView.findViewById(R.id.ivImage)
        }

        val badge: TextView = itemView.findViewById(R.id.tvBadge)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val subtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val caption: TextView = itemView.findViewById(R.id.tvCaption)
    }

    fun clicksFlow(): Flow<Model> = onItemClickSharedFlow

    data class Model(
        override val listId: String,
        val image: Int,
        val title: String,
        val subtitle: TextModel,
        val caption: String? = null,
        val badge: String? = null,
        val parcel: Any? = null
    ) : ListItem {
        override fun calculatePayload(oldItem: Any) = (oldItem as? Model)?.let {
            Payload(
                imageChanged = oldItem.image != image,
                titleChanged = oldItem.title != title,
                subtitleChanged = oldItem.subtitle != subtitle,
                captionChanged = oldItem.caption != caption,
                badgeChanged = oldItem.badge != badge
            )
        }
    }

    data class Payload(
        val imageChanged: Boolean,
        val titleChanged: Boolean,
        val subtitleChanged: Boolean,
        val captionChanged: Boolean,
        val badgeChanged: Boolean
    )

}