package com.tenesuzun.socketarge.data.websocket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import com.tenesuzun.socketarge.data.model.*
import kotlinx.serialization.encodeToString

class WebSocketManager(
//    private val serverUrl: String = "ws://localhost:8080" // Emulator için
    private val serverUrl: String = "ws://10.16.105.231:8080" // Emulator için
    // Gerçek cihaz için: "ws://YOUR_COMPUTER_IP:8080"
) {
    private val TAG = "WebSocketManager"

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Received notifications
    private val _notifications = MutableStateFlow<List<ReceivedNotification>>(emptyList())
    val notifications: StateFlow<List<ReceivedNotification>> = _notifications.asStateFlow()

    // Client ID
    private val _clientId = MutableStateFlow<Int?>(null)
    val clientId: StateFlow<Int?> = _clientId.asStateFlow()

    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun connect(deviceName: String = "Android Device") {
        if (_connectionState.value == ConnectionState.Connected ||
            _connectionState.value == ConnectionState.Connecting) {
            Log.d(TAG, "Already connected or connecting")
            return
        }

        _connectionState.value = ConnectionState.Connecting
        Log.d(TAG, "Connecting to $serverUrl")

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected!")
                _connectionState.value = ConnectionState.Connected

                // Register as mobile client
                scope.launch {
                    delay(500) // Server'ın hazır olması için kısa bir bekleme
                    val registerMessage = RegisterMessage(
                        type = "register",
                        clientType = "mobile",
                        name = deviceName
                    )
                    val jsonString = json.encodeToString(registerMessage)
                    webSocket.send(jsonString)
                    Log.d(TAG, "Sent registration: $jsonString")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code / $reason")
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error: ${t.message}", t)
                _connectionState.value = ConnectionState.Error(
                    t.message ?: "Unknown error"
                )

                // Auto-reconnect after 5 seconds
                scope.launch {
                    delay(5000)
                    if (_connectionState.value is ConnectionState.Error ||
                        _connectionState.value is ConnectionState.Disconnected) {
                        Log.d(TAG, "Attempting to reconnect...")
                        connect(deviceName)
                    }
                }
            }
        })
    }

    private fun handleIncomingMessage(text: String) {
        try {
            // First, try to determine message type
            val typeMap = json.decodeFromString<Map<String, String>>(text)
            val type = typeMap["type"]

            when (type) {
                "welcome" -> {
                    val welcome = json.decodeFromString<WelcomeMessage>(text)
                    _clientId.value = welcome.clientId
                    Log.d(TAG, "Received welcome, Client ID: ${welcome.clientId}")
                }

                "notification" -> {
                    val notification = json.decodeFromString<NotificationMessage>(text)
                    val received = ReceivedNotification(
                        message = notification.message,
                        from = notification.from,
                        timestamp = notification.timestamp
                    )

                    // Add to list
                    _notifications.value = _notifications.value + received
                    Log.d(TAG, "Notification added: ${notification.message}")
                }

                "pong" -> {
                    Log.d(TAG, "Received pong")
                }

                else -> {
                    Log.d(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}", e)
        }
    }

    fun sendMessage(message: String) {
        val chatMessage = ChatMessage(
            type = "chat",
            message = message
        )
        val jsonString = json.encodeToString(chatMessage)
        webSocket?.send(jsonString)
        Log.d(TAG, "Sent chat: $jsonString")
    }


    fun sendPing() {
        val pingMessage = PingMessage()
        val jsonString = json.encodeToString(pingMessage)
        webSocket?.send(jsonString)
        Log.d(TAG, "Sent ping")
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun cleanup() {
        disconnect()
        client.dispatcher.executorService.shutdown()
    }
}