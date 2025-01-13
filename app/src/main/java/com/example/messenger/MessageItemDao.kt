package com.example.messenger

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageItemDao {
    @Query("SELECT * FROM MessageItem")
    suspend fun getAll(): List<MessageItem>

    @Query("SELECT * FROM MessageItem " +
            "WHERE chatId LIKE :chatId")
    suspend fun getChatMessages(chatId: String): List<MessageItem>

    @Insert
    suspend fun insertAll(vararg messages: MessageItem)

    @Update
    suspend fun updateAll(vararg messages: MessageItem)

    @Delete
    suspend fun delete(message: MessageItem)
}