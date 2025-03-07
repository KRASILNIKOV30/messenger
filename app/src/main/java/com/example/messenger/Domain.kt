package com.example.messenger

data class ChatItem(
    val id: String,
    val imageUrl: String,
    val name: String,
    val message: String,
)

data class ClientMessage(
    val name: String,
    val avatarUrl: String,
    val message: String,
)

const val DEFAULT_PORT = 8085

const val DEFAULT_AVATAR_URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSTtKDBHoGq6L5htfFMFrluklPkLsQd4e3PAg&s"
const val DEFAULT_NAME = "Guest295"

const val NAME_POSTFIX = "_name"
const val AVATAR_POSTFIX = "_avatar"