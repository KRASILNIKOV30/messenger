package com.example.messenger

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
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
    private var serverSocket: ServerSocket? = null
    private var isRunning = true

    init {
        thread(start = true) {
            try {
                serverSocket = ServerSocket(port)
                Log.d("SERVER", "Server is listening on port $port")

                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    thread(start = true) {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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

    private fun handleClient(clientSocket: Socket?) {
        try {
            val clientReader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
            val clientWriter = PrintWriter(clientSocket?.getOutputStream(), true)

            var message: String? = clientReader.readLine()
            while (message != null) {
                val clientMessage = Gson().fromJson(message, ClientMessage::class.java)
                messageHandler(clientMessage.message)
                val response = ClientMessage(
                    message = "Ответ",
                    name = name,
                    avatarUrl = avatarUrl,
                )
                clientWriter.println(Gson().toJson(response))
                message = clientReader.readLine()
            }

            clientSocket?.close()
            clientReader.close()
            clientWriter.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.IO) {
            val clientMessage = ClientMessage(
                message = message,
                name = name,
                avatarUrl = avatarUrl,
            )
            val jsonMessage = Gson().toJson(clientMessage)
            writer?.println(jsonMessage)
        }
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