package com.example.messenger

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageItem(
    @PrimaryKey
    val uid: String,
    @ColumnInfo(name = "chatId")
    val chatId: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    @ColumnInfo(name = "owned")
    val owned: Boolean,
)