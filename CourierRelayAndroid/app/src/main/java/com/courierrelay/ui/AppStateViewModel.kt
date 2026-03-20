package com.courierrelay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courierrelay.data.repo.AppSettings
import com.courierrelay.data.repo.EventRepository
import com.courierrelay.data.repo.SettingsRepository
import com.courierrelay.model.NotificationEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val settings: AppSettings = AppSettings(),
    val events: List<NotificationEvent> = emptyList(),
    val packageFiltersText: String = "",
    val minConfidenceText: String = "0.85"
)

class AppStateViewModel(
    private val settingsRepository: SettingsRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    private val editablePackageFilters = MutableStateFlow("")
    private val editableConfidence = MutableStateFlow("")

    val uiState: StateFlow<AppUiState> = combine(
        settingsRepository.settingsFlow,
        eventRepository.eventsFlow,
        editablePackageFilters,
        editableConfidence
    ) { settings, events, filtersText, confidenceText ->
        AppUiState(
            settings = settings,
            events = events,
            packageFiltersText = if (filtersText.isBlank()) settings.packageFilters.joinToString(",") else filtersText,
            minConfidenceText = if (confidenceText.isBlank()) settings.minConfidence.toString() else confidenceText
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUiState())

    fun onPackageFiltersEdited(value: String) {
        editablePackageFilters.value = value
    }

    fun onMinConfidenceEdited(value: String) {
        editableConfidence.value = value
    }

    fun saveSettings(
        botToken: String,
        chatId: String,
        packageFilters: String,
        autoActionEnabled: Boolean,
        minConfidenceText: String
    ) {
        viewModelScope.launch {
            settingsRepository.save(
                botToken = botToken,
                chatId = chatId,
                packageFiltersRaw = packageFilters,
                autoActionEnabled = autoActionEnabled,
                minConfidence = minConfidenceText.toFloatOrNull() ?: 0.85f
            )
            editablePackageFilters.value = packageFilters
            editableConfidence.value = minConfidenceText
        }
    }

    fun sendMockEvent() {
        viewModelScope.launch {
            eventRepository.submitMockEvent()
        }
    }

    fun retryFailed() {
        viewModelScope.launch {
            eventRepository.retryFailedEvents()
        }
    }
}
