package com.revolut.kompot.sample.playground.delegates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.revolut.kompot.sample.playground.R
import com.revolut.kompot.sample.utils.MutableBufferedSharedFlow
import com.revolut.recyclerkit.delegates.BaseRecyclerViewDelegate
import com.revolut.recyclerkit.delegates.BaseRecyclerViewHolder
import com.revolut.recyclerkit.delegates.ListItem
import kotlinx.coroutines.flow.Flow

class ButtonDelegate : BaseRecyclerViewDelegate<ButtonDelegate.Model, ButtonDelegate.ViewHolder>(
    R.layout.delegate_button,
    { _, data -> data is Model }
) {
    private val onClickSharedFlow = MutableBufferedSharedFlow<Model>()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.delegate_button, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, data: Model, pos: Int, payloads: List<Any>?) {
        super.onBindViewHolder(holder, data, pos, payloads)

        val currentPayloads = payloads?.mapNotNull { it as? Payload }
        if (currentPayloads.isNullOrEmpty()) {
            holder.button.text = data.text
            holder.button.isEnabled = data.enabled
        } else {
            currentPayloads.forEach { payload ->
                if (payload.textChanged) {
                    holder.button.text = data.text
                }
                if (payload.enabledChanged) {
                    holder.button.isEnabled = data.enabled
                }
            }
        }

        holder.button.setOnClickListener { onClickSharedFlow.tryEmit(data) }
    }

    fun clicksFlow(): Flow<Model> = onClickSharedFlow

    class ViewHolder(itemView: View) : BaseRecyclerViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.button)
    }

    data class Model(
        override val listId: String,
        val text: String?,
        val enabled: Boolean = true
    ) : ListItem {
        override fun calculatePayload(oldItem: Any): Any? {
            return (oldItem as? Model)?.let {
                Payload(
                    textChanged = oldItem.text != text,
                    enabledChanged = oldItem.enabled != enabled
                )
            }
        }
    }

    data class Payload(
        val textChanged: Boolean,
        val enabledChanged: Boolean
    )
}