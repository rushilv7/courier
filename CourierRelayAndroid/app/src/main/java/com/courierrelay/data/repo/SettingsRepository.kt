package com.courierrelay.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val botToken: String = "",
    val chatId: String = "",
    val packageFilters: List<String> = emptyList(),
    val autoActionEnabled: Boolean = false,
    val minConfidence: Float = 0.85f
)

class SettingsRepository(private val context: Context) {
    private val botTokenKey = stringPreferencesKey("bot_token")
    private val chatIdKey = stringPreferencesKey("chat_id")
    private val packageFiltersKey = stringPreferencesKey("package_filters")
    private val autoActionEnabledKey = booleanPreferencesKey("auto_action_enabled")
    private val minConfidenceKey = floatPreferencesKey("min_confidence")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            botToken = prefs[botTokenKey].orEmpty(),
            chatId = prefs[chatIdKey].orEmpty(),
            packageFilters = prefs[packageFiltersKey]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList(),
            autoActionEnabled = prefs[autoActionEnabledKey] ?: false,
            minConfidence = prefs[minConfidenceKey] ?: 0.85f
        )
    }

    suspend fun save(
        botToken: String,
        chatId: String,
        packageFiltersRaw: String,
        autoActionEnabled: Boolean,
        minConfidence: Float
    ) {
        context.dataStore.edit { prefs ->
            prefs[botTokenKey] = botToken.trim()
            prefs[chatIdKey] = chatId.trim()
            prefs[packageFiltersKey] = packageFiltersRaw
            prefs[autoActionEnabledKey] = autoActionEnabled
            prefs[minConfidenceKey] = minConfidence.coerceIn(0.1f, 0.99f)
        }
    }
}
