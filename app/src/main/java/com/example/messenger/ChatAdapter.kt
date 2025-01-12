package com.example.messenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ChatListItemBinding

class ChatViewHolder(view: View): RecyclerView.ViewHolder(view)

class ChatAdapter(
    private val onItemClick: (ChatItem) -> Unit
): RecyclerView.Adapter<ChatViewHolder>() {
    var chatList = listOf<ChatItem>()

    override fun getItemCount(): Int = chatList.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = ChatListItemBinding.inflate(inflater, parent, false)

        return ChatViewHolder(view.root)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val itemBinding = ChatListItemBinding.bind(holder.itemView)
        val chat = chatList[position]

        itemBinding.name.text = chat.name
        itemBinding.message.text = chat.message
        Glide
            .with(holder.itemView)
            .load(chat.imageUrl)
            .centerCrop()
            .into(itemBinding.avatar)

        itemBinding.container.setOnClickListener {
            onItemClick(chat)
        }
    }
}