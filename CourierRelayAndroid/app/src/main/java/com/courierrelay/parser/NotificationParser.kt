package com.courierrelay.parser

import android.service.notification.StatusBarNotification
import com.courierrelay.model.NotificationEvent
import com.courierrelay.model.SendStatus

class NotificationParser {
    fun parse(sbn: StatusBarNotification): NotificationEvent? {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return null
        val message = extras.getCharSequence("android.text")?.toString().orEmpty()
        return NotificationEvent(
            packageName = sbn.packageName,
            appName = sbn.packageName.substringAfterLast('.'),
            title = title,
            message = message,
            createdAt = System.currentTimeMillis(),
            status = SendStatus.PENDING
        )
    }
}
