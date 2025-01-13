package com.example.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

@Database(
    entities = [
        MessageItem::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageItemDao(): MessageItemDao
}

data class ChatState(
    val messages: List<MessageItem> = listOf(),
)

class ChatViewModel(
    private val chatId: String,
    private val dao: MessageItemDao,
): ViewModel() {
    val state = MutableStateFlow(ChatState())

    init {
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            val messages = dao.getChatMessages(chatId = chatId)
            state.update { it.copy(
                messages = messages,
            ) }
        }
    }

    fun sendMessage(message: String) {
        if (message.isEmpty()) {
            return
        }
        viewModelScope.launch {
            val messageItem = MessageItem(
                uid = UUID.randomUUID().toString(),
                chatId = chatId,
                text = message,
                createdAt = Instant.now().toEpochMilli(),
                owned = true,
            )
            dao.insertAll(messageItem)
            state.update { it.copy(
                messages = it.messages + messageItem
            ) }
        }
    }
}

class ChatViewModelFactory(
    private val chatId: String,
    private val dao: MessageItemDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatId, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}