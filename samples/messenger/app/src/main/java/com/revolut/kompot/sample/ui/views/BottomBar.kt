package com.revolut.kompot.sample.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import com.revolut.kompot.sample.R
import com.revolut.kompot.sample.databinding.ViewBottomBarBinding
import com.revolut.kompot.sample.utils.MutableBufferedSharedFlow
import com.revolut.kompot.sample.utils.children
import kotlinx.coroutines.flow.Flow

class BottomBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val binding = ViewBottomBarBinding.inflate(LayoutInflater.from(context), this, true)

    private val selectedItemSharedFlow by lazy(LazyThreadSafetyMode.NONE) { MutableBufferedSharedFlow<String>() }

    init {
        elevation = context.resources.getDimension(R.dimen.bottom_bar_elevation)
        View.inflate(context, R.layout.view_bottom_bar, this)
    }

    fun setItems(items: List<Item>) {
        binding.contentLayout.removeAllViews()
        items.forEach { item ->
            val itemView = createItemView()
            bindItemView(itemView, item)
            binding.contentLayout.addView(itemView)
        }
    }

    fun setSelected(itemId: String) {
        binding.contentLayout.children.forEach { itemView ->
            itemView.isSelected = itemView.tag == itemId
        }
    }

    fun selectedItemFlow(): Flow<String> = selectedItemSharedFlow

    private fun createItemView(): View =
        LayoutInflater.from(context)
            .inflate(R.layout.view_bottom_bar_item, binding.contentLayout, false)

    private fun bindItemView(itemView: View, item: Item) {
        itemView.tag = item.id
        itemView.findViewById<ImageView>(R.id.ivIcon).setImageResource(item.icon)
        itemView.setOnClickListener {
            setSelected(item.id)
            selectedItemSharedFlow.tryEmit(item.id)
        }
    }

    data class Item(
        val id: String,
        @DrawableRes val icon: Int
    )

}