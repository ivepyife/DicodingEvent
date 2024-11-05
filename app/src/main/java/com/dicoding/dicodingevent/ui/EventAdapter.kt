package com.dicoding.dicodingevent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.dicodingevent.R
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.databinding.EventItemBinding

class EventAdapter : ListAdapter<EventEntity, EventAdapter.EventViewHolder>(DIFF_CALLBACK) {

    private var onItemClickCallback: ((EventEntity) -> Unit)? = null
    private var onFavoriteClickCallback: ((EventEntity) -> Unit)? = null

    fun setOnItemClickCallback(callback: (EventEntity) -> Unit) {
        onItemClickCallback = callback
    }

    fun setOnFavoriteClickCallback(callback: (EventEntity) -> Unit) {
        onFavoriteClickCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)

        holder.itemView.setOnClickListener {
            onItemClickCallback?.invoke(event)
        }

        holder.binding.ivBookmark.setOnClickListener {
            onFavoriteClickCallback?.invoke(event)
            // Notify the adapter of the change to update the favorite icon in real time
            notifyItemChanged(position)
        }
    }

    class EventViewHolder(val binding: EventItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventEntity) {
            binding.apply {
                tvEventName.text = event.name
                Glide.with(itemView.context)
                    .load(event.mediaCover)
                    .into(imgEventLogo)

                updateFavoriteIcon(event.isFavorite)
            }
        }

        // Helper function to update the favorite icon
        private fun updateFavoriteIcon(isFavorite: Boolean) {
            val bookmarkDrawable = if (isFavorite) {
                R.drawable.baseline_favorite_24
            } else {
                R.drawable.baseline_favorite_border_24
            }
            binding.ivBookmark.setImageDrawable(
                ContextCompat.getDrawable(binding.ivBookmark.context, bookmarkDrawable)
            )
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EventEntity>() {
            override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
