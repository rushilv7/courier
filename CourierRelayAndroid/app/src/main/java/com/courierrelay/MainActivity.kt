package com.courierrelay

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.courierrelay.ui.AppStateViewModel
import com.courierrelay.ui.Screens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: AppStateViewModel = viewModel(factory = SimpleViewModelFactory())
                App(vm)
            }
        }
    }
}

@Composable
private fun App(viewModel: AppStateViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val screens = remember { Screens.entries }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            screens.forEach {
                Button(onClick = {
                    when (it) {
                        Screens.Onboarding -> {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                        Screens.Settings -> {}
                        Screens.Events -> {}
                    }
                }) {
                    Text(it.name)
                }
            }
        }

        Text("Onboarding: enable Notification Access + Accessibility for CourierRelay.")

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.settings.botToken,
            onValueChange = { viewModel.saveSettings(it, uiState.settings.chatId, uiState.packageFiltersText, uiState.settings.autoActionEnabled, uiState.minConfidenceText) },
            label = { Text("Telegram Bot Token") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.settings.chatId,
            onValueChange = { viewModel.saveSettings(uiState.settings.botToken, it, uiState.packageFiltersText, uiState.settings.autoActionEnabled, uiState.minConfidenceText) },
            label = { Text("Telegram Chat ID") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.packageFiltersText,
            onValueChange = { viewModel.onPackageFiltersEdited(it) },
            label = { Text("Package filters (comma-separated)") }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Enable optional auto-accept action")
            Switch(
                checked = uiState.settings.autoActionEnabled,
                onCheckedChange = {
                    viewModel.saveSettings(
                        uiState.settings.botToken,
                        uiState.settings.chatId,
                        uiState.packageFiltersText,
                        it,
                        uiState.minConfidenceText
                    )
                }
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.minConfidenceText,
            onValueChange = { viewModel.onMinConfidenceEdited(it) },
            label = { Text("Auto-action confidence threshold (0.1-0.99)") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.saveSettings(
                    uiState.settings.botToken,
                    uiState.settings.chatId,
                    uiState.packageFiltersText,
                    uiState.settings.autoActionEnabled,
                    uiState.minConfidenceText
                )
            }) { Text("Save Settings") }
            Button(onClick = viewModel::sendMockEvent) { Text("Send Mock Event") }
            Button(onClick = viewModel::retryFailed) { Text("Retry Failed") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.events) { event ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${event.appName} (${event.packageName})")
                        Text(event.title)
                        Text(event.message)
                        Text("Status: ${event.status}")
                        event.errorMessage?.let { Text("Error: $it") }
                    }
                }
            }
        }
    }
}
