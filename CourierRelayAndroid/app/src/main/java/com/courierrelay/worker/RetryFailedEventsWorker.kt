package com.courierrelay.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.courierrelay.CourierRelayApplication

class RetryFailedEventsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            CourierRelayApplication.container.eventRepository.retryFailedEvents()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
