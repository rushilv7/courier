package com.courierrelay.model

import com.courierrelay.data.local.EventEntity

enum class SendStatus {
    PENDING,
    SENT,
    FAILED
}

data class NotificationEvent(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val createdAt: Long,
    val status: SendStatus,
    val errorMessage: String? = null
)

fun NotificationEvent.toEntity(): EventEntity = EventEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    title = title,
    message = message,
    createdAt = createdAt,
    status = status.name,
    errorMessage = errorMessage
)

fun EventEntity.toModel(): NotificationEvent = NotificationEvent(
    id = id,
    packageName = packageName,
    appName = appName,
    title = title,
    message = message,
    createdAt = createdAt,
    status = SendStatus.valueOf(status),
    errorMessage = errorMessage
)
