package com.example.messenger

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class P2PConnection(
    private val ip: String,
    private val port: Int,
    private val name: String,
    private val avatarUrl: String,
    private val messageHandler: (String) -> Unit,
) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    init {
        thread(start = true) {
            try {
                socket = Socket(ip, port)
                reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                writer = PrintWriter(socket?.getOutputStream(), true)

                listenForMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(message: String) {
        val clientMessage = ClientMessage(
            message = message,
            name = name,
            avatarUrl = avatarUrl,
        )
        val jsonMessage = Gson().toJson(clientMessage)
        writer?.println(jsonMessage)
    }

    private fun listenForMessages() {
        try {
            while (true) {
                val message = reader?.readLine()
                message?.let {
                    val clientMessage = Gson().fromJson(it, ClientMessage::class.java)
                    messageHandler(clientMessage.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            socket?.close()
            reader?.close()
            writer?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}