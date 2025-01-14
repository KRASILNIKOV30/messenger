package com.example.messenger
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import kotlin.concurrent.thread

data class State(
    val chats: List<ChatItem> = listOf(),
    val isChatsSelected: Boolean = true,
    val name: String = DEFAULT_NAME,
    val avatarUrl: String = DEFAULT_AVATAR_URL,
    val isInput: Boolean = false,
)

sealed interface Event {
    data class GoToChat(val chat: ChatItem) : Event
}

class MainFragmentViewModel(
    private val dao: MessageItemDao,
) : ViewModel() {
    val state = MutableStateFlow(State())
    val event = MutableSharedFlow<Event>()
    val listener = P2PListener(DEFAULT_PORT) { chatId, message ->
        receiveMessage(chatId, message)
    }

//    init {
//        val db = StorageApp.db
//        thread(start = true) {
//            db.clearAllTables()
//        }
//    }

    fun onMenuItemReselected(item: MenuItem) {
        state.update { it.copy(
            isChatsSelected = item.itemId == R.id.chats_menu_item
        )}
    }

    fun onCommitIp(ip: String) {
        val message = ClientMessage(
            name = ip,
            avatarUrl = DEFAULT_AVATAR_URL,
            message = "",
        )
        updateChats(ip, message)
        onCloseInput()
    }

    fun onGoToChat() {
        listener.stopListening()
    }

    fun onAddCompanion() {
        state.update { it.copy(
            isInput = true,
        ) }
    }

    fun onCloseInput() {
        state.update { it.copy(
            isInput = false
        ) }
    }

    private fun receiveMessage(chatId: String, message: ClientMessage) {
        viewModelScope.launch {
            val messageItem = MessageItem(
                uid = UUID.randomUUID().toString(),
                chatId = chatId,
                text = message.message,
                createdAt = Instant.now().toEpochMilli(),
                owned = false,
            )
            dao.insertAll(messageItem)
            updateChats(chatId, message)
        }
    }

    private fun updateChats(chatId: String, message: ClientMessage) {
        val chat = state.value.chats.find { it.id == chatId }
        if (chat == null) {
            val newChat = ChatItem(
                id = chatId,
                name = message.name,
                imageUrl = message.avatarUrl,
                message = message.message
            )
            state.update { it.copy(
                chats = it.chats + newChat
            ) }
        } else {
            state.update { it.copy(
                chats = it.chats.map { chat ->
                    if (chat.id == chatId) {
                        chat.copy(message = message.message)
                    } else {
                        chat
                    }
                }
            ) }
        }
    }
}

class MainFragmentViewModelFactory(
    private val dao: MessageItemDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
            return MainFragmentViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}