package com.example.messenger

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull

class SettingsStorage(
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val nameKey = stringPreferencesKey("name_key")
    private val avatarKey = stringPreferencesKey("avatar_key")

    suspend fun saveName(name: String) {
        context.dataStore.edit {
            it[nameKey] = name
        }
    }

    suspend fun saveChatName(chatId: String, name: String) {
        val key = stringPreferencesKey(chatId)
        context.dataStore.edit {
            it[key] = name
        }
    }

    suspend fun saveAvatar(avatar: String) {
        context.dataStore.edit {
            it[avatarKey] = avatar
        }
    }

    suspend fun saveChatAvatar(chatId: String, avatar: String) {
        val key = stringPreferencesKey(chatId)
        context.dataStore.edit {
            it[key] = avatar
        }
    }

    suspend fun getName(): String? {
        return context.dataStore.data.firstOrNull()?.get(nameKey)
    }

    suspend fun getChatName(chatId: String): String? {
        val key = stringPreferencesKey(chatId)
        return context.dataStore.data.firstOrNull()?.get(key)
    }

    suspend fun getAvatar(): String? {
        return context.dataStore.data.firstOrNull()?.get(avatarKey)
    }

    suspend fun getChatAvatar(chatId: String): String? {
        val key = stringPreferencesKey(chatId)
        return context.dataStore.data.firstOrNull()?.get(key)
    }
}