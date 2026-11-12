package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.model.local.data.Chat
import com.codelabs.gardahijau.model.local.data.Author

class ChatAdapter : ListAdapter<Chat, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromMe) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val v = inflater.inflate(R.layout.item_chat_sent, parent, false)
            SentViewHolder(v)
        } else {
            val v = inflater.inflate(R.layout.item_chat_received, parent, false)
            ReceivedViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = getItem(position)
        when (holder) {
            is SentViewHolder -> holder.bind(chat)
            is ReceivedViewHolder -> holder.bind(chat)
        }
    }

    fun addMessage(chat: Chat) {
        val newList = currentList.toMutableList()
        newList.add(chat)
        submitList(newList)
    }

    private class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(chat: Chat) {
            tvMessage.text = chat.text
        }
    }

    private class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(chat: Chat) {
            tvMessage.text = chat.text
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean =
            oldItem.timestamp == newItem.timestamp

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean =
            oldItem == newItem
    }
}