package com.example.messenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.databinding.MessageItemBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageViewHolder(view: View): RecyclerView.ViewHolder(view)

class MessageAdapter: RecyclerView.Adapter<MessageViewHolder>() {
    var messageList = listOf<MessageItem>()

    override fun getItemCount(): Int = messageList.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = MessageItemBinding.inflate(inflater, parent, false)

        return MessageViewHolder(view.root)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val itemBinding = MessageItemBinding.bind(holder.itemView)
        val message = messageList[position]
        val formattedTime = formatTimestamp(message.createdAt)

        itemBinding.senderMessage.isInvisible = !message.owned
        itemBinding.receiverMessage.isInvisible = message.owned
        itemBinding.senderMessageText.text = message.text
        itemBinding.receiverMessageText.text = message.text
        itemBinding.senderMessageDate.text = formattedTime
        itemBinding.receiverMessageDate.text = formattedTime
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        return format.format(date)
    }
}