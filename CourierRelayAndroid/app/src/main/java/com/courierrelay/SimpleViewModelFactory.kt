package com.courierrelay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.courierrelay.ui.AppStateViewModel

class SimpleViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppStateViewModel::class.java)) {
            return AppStateViewModel(
                CourierRelayApplication.container.settingsRepository,
                CourierRelayApplication.container.eventRepository
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
