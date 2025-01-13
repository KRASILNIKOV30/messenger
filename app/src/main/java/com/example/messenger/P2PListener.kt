package com.example.messenger

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import com.google.gson.Gson

class P2PListener(
    private val port: Int,
    private val messageHandler: (String, ClientMessage) -> Unit,
) {
    private var serverSocket: ServerSocket? = null

    init {
        thread(start = true) {
            try {
                serverSocket = ServerSocket(port)

                while (true) {
                    val clientSocket = serverSocket?.accept()
                    thread(start = true) {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(clientSocket: Socket?) {
        try {
            val inputStream = clientSocket?.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val ipAddress = clientSocket?.inetAddress?.hostAddress

            val message = reader.readLine()

            message?.let {
                val clientMessage = Gson().fromJson(it, ClientMessage::class.java)
                messageHandler(ipAddress ?: "Unknown", clientMessage)
            }

            reader.close()
            clientSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
