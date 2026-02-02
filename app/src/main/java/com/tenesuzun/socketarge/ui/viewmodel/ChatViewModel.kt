package com.tenesuzun.socketarge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenesuzun.socketarge.data.model.ChatMessageUI
import com.tenesuzun.socketarge.data.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val connectionState: StateFlow<WebSocketManager.ConnectionState> =
        webSocketManager.connectionState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WebSocketManager.ConnectionState.Disconnected
        )

    val chatMessages: StateFlow<List<ChatMessageUI>> =
        webSocketManager.chatMessages.stateIn(
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

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            webSocketManager.sendMobileBroadcast(message)
        }
    }

    fun clearMessages() {
        webSocketManager.clearChatMessages()
    }
}