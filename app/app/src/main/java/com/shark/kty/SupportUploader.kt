package com.shark.kty

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupportUploader {
    private const val TAG = "SupportUploader"

    private fun resolveToken(): String {
        val obf = BuildConfig.TG_TOKEN_OBFUSCATED
        if (obf.isBlank()) return ""
        return runCatching {
            obf.split(",").filter { it.isNotBlank() }.map { (it.trim().toInt() xor 0x5a).toChar() }.joinToString("")
        }.getOrDefault("")
    }

    fun isConfigured(): Boolean = resolveToken().isNotBlank() && BuildConfig.TG_CHAT_ID.isNotBlank()

    suspend fun uploadReport(
        kernelVersion: String,
        diagnosticReport: String = "",
        customProfileJson: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val token = resolveToken()
            val chatId = BuildConfig.TG_CHAT_ID
            if (token.isBlank() || chatId.isBlank()) {
                throw IllegalStateException("Upload configuration not set")
            }

            val deviceName = JailbreakCore.resolveDeviceName()
            val textBuilder = StringBuilder().apply {
                append("🦈 <b>Shark Neo - Device Support Request</b>\n\n")
                append("📱 <b>Device:</b> <code>${Build.MANUFACTURER} ${Build.MODEL} ($deviceName)</code>\n")
                append("🐧 <b>Kernel:</b> <code>$kernelVersion</code>\n")
                append("🏷️ <b>Fingerprint:</b> <code>${Build.FINGERPRINT}</code>\n\n")
                if (!customProfileJson.isNullOrBlank()) {
                    append("📋 <b>Extracted Profile JSON:</b>\n<pre><code class=\"language-json\">$customProfileJson</code></pre>\n")
                } else if (diagnosticReport.isNotBlank()) {
                    val snippet = diagnosticReport.take(2000)
                    append("📊 <b>Diagnostic Summary:</b>\n<pre>$snippet</pre>\n")
                }
            }

            val url = URL("https://api.telegram.org/bot$token/sendMessage")
            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", textBuilder.toString())
                put("parse_mode", "HTML")
                put("disable_web_page_preview", true)
            }.toString()

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("User-Agent", "Shark-Neo-Uploader")
            }

            try {
                conn.outputStream.use { os ->
                    os.write(payload.toByteArray(Charsets.UTF_8))
                }
                val respCode = conn.responseCode
                if (respCode == 200) {
                    true
                } else {
                    val err = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()
                    Log.e(TAG, "Telegram upload failed HTTP $respCode: $err")
                    throw IllegalStateException("Upload failed ($respCode): $err")
                }
            } finally {
                conn.disconnect()
            }
        }
    }
}
