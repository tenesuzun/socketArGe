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

// Alınacak mesajlar
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

@Serializable
data class ChatMessage(
    val type: String = "chat",
    val message: String
)

// UI için kullanacağımız data class
data class ReceivedNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val from: String,
    val timestamp: String,
    val receivedAt: Long = System.currentTimeMillis()
)