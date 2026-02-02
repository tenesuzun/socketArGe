package com.tenesuzun.socketarge.data.model

import kotlinx.serialization.Serializable

// Gönderilecek mesajlar
/**
 * RegisterMessage'da type = "register" default value olarak tanımlı.
 * Kotlinx Serialization varsayılan olarak default değerleri JSON'a dahil etmiyor (bandwidth tasarrufu için).
 * encodeDefaults = true ekleyince tüm alanlar serialize edilir.
 */
@Serializable
data class RegisterMessage(
    val type: String = "register",
    val clientType: String,
    val name: String
)

@Serializable
data class PingMessage(
    val type: String = "ping"
)

// Senaryo 1: Mobile → Admin (response/feedback)
@Serializable
data class ResponseMessage(
    val type: String = "response",
    val message: String,
    val targetType: String = "admin"
)

// Senaryo 2: Mobile → Tüm Mobile'lar (broadcast)
@Serializable
data class MobileBroadcastMessage(
    val type: String = "mobile_broadcast",
    val message: String
)

// Senaryo 3: Mobile → Belirli Client (direct message)
@Serializable
data class DirectMessage(
    val type: String = "direct",
    val targetId: Int,
    val message: String
)

// ==================== ALINACAK MESAJLAR ====================

@Serializable
data class WelcomeMessage(
    val type: String,
    val message: String,
    val clientId: Int
)

@Serializable
data class NotificationMessage(
    val type: String,
    val message: String,
    val from: String,
    val timestamp: String
)

@Serializable
data class PongMessage(
    val type: String
)

// Mobile broadcast alındığında
@Serializable
data class MobileBroadcastReceived(
    val type: String,
    val message: String,
    val from: String,
    val fromId: Int,
    val timestamp: String
)

// Direct message alındığında
@Serializable
data class DirectMessageReceived(
    val type: String,
    val message: String,
    val from: String,
    val fromId: Int,
    val timestamp: String
)

// Admin'den client listesi alındığında (Senaryo 3 için)
@Serializable
data class ClientListMessage(
    val type: String,
    val clients: List<ClientInfo>
)

@Serializable
data class ClientInfo(
    val id: Int,
    val name: String,
    val type: String,
    val status: String
)

// ==================== UI MODELLERİ ====================

data class ReceivedNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val from: String,
    val timestamp: String,
    val receivedAt: Long = System.currentTimeMillis()
)

data class ChatMessageUI(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val from: String,
    val fromId: Int,
    val timestamp: String,
    val isMine: Boolean = false
)