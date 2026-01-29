package com.tenesuzun.socketarge.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tenesuzun.socketarge.data.model.ReceivedNotification
import com.tenesuzun.socketarge.data.websocket.WebSocketManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val webSocketManager = WebSocketManager()

    val connectionState: StateFlow<WebSocketManager.ConnectionState> =
        webSocketManager.connectionState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WebSocketManager.ConnectionState.Disconnected
        )

    val notifications: StateFlow<List<ReceivedNotification>> =
        webSocketManager.notifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val clientId: StateFlow<Int?> =
        webSocketManager.clientId.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Otomatik bağlan
        connectToServer()
    }

    fun connectToServer() {
        viewModelScope.launch {
            val deviceName = "Android ${android.os.Build.MODEL}"
            webSocketManager.connect(deviceName)
        }
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }

    fun clearNotifications() {
        webSocketManager.clearNotifications()
    }

    fun sendPing() {
        webSocketManager.sendPing()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.cleanup()
    }
}