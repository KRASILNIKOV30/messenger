package com.example.messenger
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.view.MenuItem
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class State(
    val chats: List<ChatItem> = listOf(),
    val isChatsSelected: Boolean = true,
)

class MainFragmentViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    fun onAddChat() {
//        state.update { it.copy(
//            chats = it.chats + ChatItem(
//                id = "1",
//                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d4/S%C3%B8ren_Kierkegaard_%281813-1855%29_-_%28cropped%29.jpg",
//                name = "Søren Kierkegaard",
//                message = "Неужели чудотворец - лишь продукт устного творчества?",
//            )
//        ) }
    }

    fun onMenuItemReselected(item: MenuItem) {
        state.update { it.copy(
            isChatsSelected = item.itemId == R.id.chats_menu_item
        )}
    }
}