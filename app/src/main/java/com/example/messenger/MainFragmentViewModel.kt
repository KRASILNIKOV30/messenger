package com.example.messenger
import android.app.Application
import android.view.MenuItem
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    application: Application,
    private val dao: MessageItemDao,
) : AndroidViewModel(application) {
    val state = MutableStateFlow(State())
    val event = MutableSharedFlow<Event>()
    val listener = P2PListener(DEFAULT_PORT) { chatId, message ->
        receiveMessage(chatId, message)
    }
    private val settingsStorage = SettingsStorage(getApplication<Application>().applicationContext)

//    init {
//        val db = StorageApp.db
//        thread(start = true) {
//            db.clearAllTables()
//        }
//    }

    init {
        loadChats()
    }

    fun onCreate() {
        viewModelScope.launch {
            val name = settingsStorage.getName() ?: DEFAULT_NAME
            val avatarUrl = settingsStorage.getAvatar() ?: DEFAULT_AVATAR_URL
            state.update { it.copy(
                name = name,
                avatarUrl = avatarUrl
            ) }
        }
    }

    fun onChangeName(name: String) {
        if (name.isEmpty()) {
            return
        }
        viewModelScope.launch {
            settingsStorage.saveName(name)
            state.update { it.copy(
                name = name
            ) }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            val messages = dao.getAll()
            var chats = mutableListOf<ChatItem>()
            messages.forEach {
                val name = settingsStorage.getChatName(it.chatId + NAME_POSTFIX) ?: DEFAULT_NAME
                val avatarUrl = settingsStorage.getChatAvatar(it.chatId + AVATAR_POSTFIX) ?: DEFAULT_AVATAR_URL
                val clientMessage = ClientMessage(
                    message = it.text,
                    name = name,
                    avatarUrl = avatarUrl,
                )
                updateChats(it.chatId, clientMessage)
            }
        }
    }

    fun onChangeAvatar(avatarUrl: String) {
        if (avatarUrl.isEmpty()) {
            return
        }
        viewModelScope.launch {
            settingsStorage.saveAvatar(avatarUrl)
            state.update { it.copy(
                avatarUrl = avatarUrl
            ) }
        }
    }

    fun onMenuItemReselected(item: MenuItem) {
        state.update { it.copy(
            isChatsSelected = item.itemId == R.id.chats_menu_item
        )}
    }

    fun deleteAllMessages() {
        thread(start = true) {
            val db = StorageApp.db
            db.clearAllTables()
            state.update { it.copy(
                chats = listOf()
            ) }
        }
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
        viewModelScope.launch {
            settingsStorage.saveChatName(chatId + NAME_POSTFIX, message.name)
            settingsStorage.saveChatAvatar(chatId + AVATAR_POSTFIX, message.avatarUrl)
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
                            chat.copy(
                                message = message.message,
                                name = message.name,
                                imageUrl = message.avatarUrl,
                            )
                        } else {
                            chat
                        }
                    }
                ) }
            }
        }
    }
}

class MainFragmentViewModelFactory(
    private val application: Application,
    private val dao: MessageItemDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
            return MainFragmentViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}