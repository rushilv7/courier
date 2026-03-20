package com.courierrelay.data.repo

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.courierrelay.data.local.EventDao
import com.courierrelay.model.NotificationEvent
import com.courierrelay.model.SendStatus
import com.courierrelay.model.toEntity
import com.courierrelay.model.toModel
import com.courierrelay.telegram.TelegramClient
import com.courierrelay.worker.RetryFailedEventsWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EventRepository(
    private val eventDao: EventDao,
    private val settingsRepository: SettingsRepository,
    private val telegramClient: TelegramClient,
    private val workManager: WorkManager
) {
    val eventsFlow: Flow<List<NotificationEvent>> = eventDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun captureAndSend(event: NotificationEvent) {
        val id = eventDao.insert(event.toEntity())
        sendEvent(id)
    }

    suspend fun sendEvent(id: Long) {
        val entity = eventDao.getById(id) ?: return
        val settings = settingsRepository.settingsFlow.first()
        val text = "🚚 ${entity.appName}\n${entity.title}\n${entity.message}"
        val result = telegramClient.sendMessage(settings.botToken, settings.chatId, text)

        val updated = entity.copy(
            status = if (result.isSuccess) SendStatus.SENT.name else SendStatus.FAILED.name,
            errorMessage = result.exceptionOrNull()?.message
        )
        eventDao.update(updated)

        if (result.isFailure) {
            enqueueRetry()
        }
    }

    suspend fun retryFailedEvents() {
        val failed = eventDao.getByStatus(SendStatus.FAILED.name)
        failed.forEach { sendEvent(it.id) }
    }

    fun enqueueRetry() {
        workManager.enqueueUniqueWork(
            "retry_failed_events",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RetryFailedEventsWorker>().build()
        )
    }

    suspend fun submitMockEvent() {
        val mock = NotificationEvent(
            packageName = "com.mock.delivery",
            appName = "mock",
            title = "Order picked up",
            message = "Rider is on the way",
            createdAt = System.currentTimeMillis(),
            status = SendStatus.PENDING
        )
        captureAndSend(mock)
    }
}
