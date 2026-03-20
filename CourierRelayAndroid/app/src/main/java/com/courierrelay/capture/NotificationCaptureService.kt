package com.courierrelay.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.courierrelay.CourierRelayApplication
import com.courierrelay.parser.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationCaptureService : NotificationListenerService() {
    private val parser = NotificationParser()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            val settings = CourierRelayApplication.container.settingsRepository.settingsFlow.first()
            if (settings.packageFilters.isNotEmpty() && sbn.packageName !in settings.packageFilters) {
                return@launch
            }

            val event = parser.parse(sbn) ?: return@launch
            CourierRelayApplication.container.eventRepository.captureAndSend(event)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
