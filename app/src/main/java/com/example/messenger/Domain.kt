package com.example.messenger

data class ChatItem(
    val id: String,
    val imageUrl: String,
    val name: String,
    val message: String,
)

data class MessageItem(
    val chatId: String,
    val text: String,
    val time: Long,
    val owned: Boolean,
)