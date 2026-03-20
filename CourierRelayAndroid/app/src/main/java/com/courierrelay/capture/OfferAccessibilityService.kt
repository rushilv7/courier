package com.courierrelay.capture

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.courierrelay.CourierRelayApplication
import com.courierrelay.model.NotificationEvent
import com.courierrelay.model.SendStatus
import com.courierrelay.parser.OfferNodeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OfferAccessibilityService : AccessibilityService() {
    private val parser = OfferNodeParser()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastHandledAtMs: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val now = System.currentTimeMillis()
        if (now - lastHandledAtMs < 500) return

        val pkg = event?.packageName?.toString().orEmpty()
        if (pkg.isBlank()) return

        scope.launch {
            val settings = CourierRelayApplication.container.settingsRepository.settingsFlow.first()
            if (settings.packageFilters.isNotEmpty() && pkg !in settings.packageFilters) return@launch

            val root = rootInActiveWindow ?: return@launch
            val parsed = parser.parse(root) ?: return@launch
            if (parsed.confidence < 0.45f) return@launch

            lastHandledAtMs = now
            val title = listOfNotNull(parsed.payout, parsed.duration, parsed.distance).joinToString(" • ")
                .ifBlank { "Offer detected" }
            val message = parsed.toRelayMessage() + "\n\nRaw:\n" + parsed.rawLines.joinToString("\n")

            CourierRelayApplication.container.eventRepository.captureAndSend(
                NotificationEvent(
                    packageName = pkg,
                    appName = pkg.substringAfterLast('.'),
                    title = title,
                    message = message,
                    createdAt = now,
                    status = SendStatus.PENDING
                )
            )

            if (settings.autoActionEnabled && parsed.confidence >= settings.minConfidence) {
                parser.findAcceptNode(root)?.let { acceptNode ->
                    AccessibilityNodeInfoCompat.wrap(acceptNode)
                        .performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
