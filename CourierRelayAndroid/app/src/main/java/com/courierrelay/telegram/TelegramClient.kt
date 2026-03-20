package com.courierrelay.telegram

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class TelegramClient(private val okHttpClient: OkHttpClient = OkHttpClient()) {
    fun sendMessage(botToken: String, chatId: String, text: String): Result<Unit> {
        if (botToken.isBlank() || chatId.isBlank()) {
            return Result.failure(IllegalStateException("Bot token/chat ID missing"))
        }
        val payload = "{\"chat_id\":\"$chatId\",\"text\":${text.asJsonString()},\"disable_web_page_preview\":true}"
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$botToken/sendMessage")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Telegram error: HTTP ${response.code} ${response.body?.string().orEmpty()}")
                }
            }
        }
    }

    private fun String.asJsonString(): String {
        return "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    }
}
