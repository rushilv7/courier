package com.courierrelay

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.courierrelay.data.local.AppDatabase
import com.courierrelay.data.repo.EventRepository
import com.courierrelay.data.repo.SettingsRepository
import com.courierrelay.telegram.TelegramClient

class CourierRelayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    class AppContainer(app: Application) {
        private val database = Room.databaseBuilder(app, AppDatabase::class.java, "courier-relay.db").build()
        val settingsRepository = SettingsRepository(app)
        private val telegramClient = TelegramClient()
        private val workManager = WorkManager.getInstance(app)
        val eventRepository = EventRepository(
            eventDao = database.eventDao(),
            settingsRepository = settingsRepository,
            telegramClient = telegramClient,
            workManager = workManager
        )
    }

    companion object {
        lateinit var container: AppContainer
            private set
    }
}
