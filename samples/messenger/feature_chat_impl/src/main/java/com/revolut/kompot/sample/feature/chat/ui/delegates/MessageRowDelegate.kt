package com.revolut.kompot.sample.feature.chat.ui.delegates

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.revolut.kompot.sample.feature.chat.R
import com.revolut.recyclerkit.delegates.BaseRecyclerViewDelegate
import com.revolut.recyclerkit.delegates.BaseRecyclerViewHolder
import com.revolut.recyclerkit.delegates.ListItem

class MessageRowDelegate : BaseRecyclerViewDelegate<MessageRowDelegate.Model, MessageRowDelegate.ViewHolder>(
    R.layout.delegate_message,
    { _, data -> data is Model }
) {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.delegate_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, data: Model, pos: Int, payloads: List<Any>?) {
        super.onBindViewHolder(holder, data, pos, payloads)

        if (payloads.isNullOrEmpty()) {
            holder.text.text = data.text
            holder.caption.text = data.caption
            holder.setGravity(data.gravity)
            holder.setBackground(data.background)
        } else {
            payloads.filterIsInstance(Payload::class.java).forEach { payload ->
                if (payload.textChanged) {
                    holder.text.text = data.text
                }
                if (payload.captionChanged) {
                    holder.caption.text = data.caption
                }
                if (payload.gravityChanged) {
                    holder.setGravity(data.gravity)
                }
                if (payload.backgroundChanged) {
                    holder.setBackground(data.background)
                }
            }
        }
    }

    class ViewHolder(itemView: View) : BaseRecyclerViewHolder(itemView) {
        private val layoutMessage: ConstraintLayout = itemView.findViewById(R.id.layoutMessage)
        val text: TextView = itemView.findViewById(R.id.tvText)
        val caption: TextView = itemView.findViewById(R.id.tvCaption)

        fun setGravity(gravity: Model.Gravity) {
            val viewGravity = when (gravity) {
                Model.Gravity.START -> Gravity.START
                Model.Gravity.END -> Gravity.END
            }
            val newParams = (layoutMessage.layoutParams as FrameLayout.LayoutParams).apply {
                this.gravity = viewGravity
            }
            layoutMessage.layoutParams = newParams
        }

        fun setBackground(background: Model.Background) {
            layoutMessage.setBackgroundResource(background.drawableRes)
        }
    }

    data class Model(
        override val listId: String,
        val text: String,
        val caption: String,
        val gravity: Gravity,
        val background: Background
    ) : ListItem {

        override fun calculatePayload(oldItem: Any) = (oldItem as? Model)?.let {
            Payload(
                textChanged = oldItem.text != text,
                captionChanged = oldItem.caption != caption,
                gravityChanged = oldItem.gravity != gravity,
                backgroundChanged = oldItem.background != background
            )
        }

        enum class Gravity {
            START, END
        }

        enum class Background(
            @DrawableRes val drawableRes: Int
        ) {
            DEFAULT(drawableRes = R.drawable.bg_message_bubble),
            DARK(drawableRes = R.drawable.bg_message_bubble_dark);
        }

    }

    data class Payload(
        val textChanged: Boolean,
        val captionChanged: Boolean,
        val gravityChanged: Boolean,
        val backgroundChanged: Boolean
    )

}