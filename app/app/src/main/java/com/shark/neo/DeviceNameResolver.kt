package com.shark.neo

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object DeviceNameResolver {
    private const val TAG = "DeviceNameResolver"
    private const val PREFS_NAME = "shark_device_cache"
    private const val PREF_KEY_MARKET_NAME = "cached_market_device_name"

    @Volatile
    private var cachedDeviceName: String? = null

    private val MARKETING_KEYWORDS = setOf(
        "find", "reno", "turbo", "k14", "k13", "k12", "k70", "k80", "note", "pro",
        "plus", "ultra", "gt", "neo", "ace", "edge", "razr", "magic", "pad", "fold",
        "flip", "x200", "x100", "iqoo", "narzo", "civi", "redmi", "poco", "axon"
    )

    private val NOISE_REGEX = Regex(
        "(?i)\\b(price|prices|specs?|specification|specifications|review|reviews|gsmarena|" +
            "details|cover|case|screen|protector|battery|display|firmware|test point|isp pinout|" +
            "pinout|testpoint|isp|unbrick|edl|rom|root|twrp|folder|model name|model|smartphone|" +
            "mobile|phone|android \\d+|512gb|256gb|128gb|64gb|in india|in bangladesh|in pakistan|" +
            "flipkart|amazon|buy|online|january|february|march|april|may|june|july|august|september|" +
            "october|november|december|\\d{4}|amoled|oled|ips|lcd|hz|mah|mp|ppi)\\b"
    )

    fun getCachedOrLocalDeviceName(context: Context? = null): String {
        cachedDeviceName?.let { if (it.isNotBlank()) return it }

        if (context != null) {
            runCatching {
                val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val saved = sp.getString(PREF_KEY_MARKET_NAME, null)
                if (!saved.isNullOrBlank() && !isRawModelOnly(saved)) {
                    cachedDeviceName = saved
                    return saved
                }
            }
        }

        val localProp = readLocalMarketProperties()
        if (localProp.isNotBlank() && !isRawModelOnly(localProp)) {
            cachedDeviceName = localProp
            saveToCache(context, localProp)
            return localProp
        }

        val fallback = buildString {
            append(Build.MANUFACTURER)
            if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) {
                append(" ").append(Build.BRAND)
            }
            append(" ").append(Build.MODEL)
        }
        return fallback
    }

    suspend fun resolveDeviceNameOnline(
        context: Context? = null,
        forceRefresh: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val existing = cachedDeviceName
            if (!existing.isNullOrBlank() && !isRawModelOnly(existing)) {
                return@withContext existing
            }
            if (context != null) {
                val saved = runCatching {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString(PREF_KEY_MARKET_NAME, null)
                }.getOrNull()
                if (!saved.isNullOrBlank() && !isRawModelOnly(saved)) {
                    cachedDeviceName = saved
                    return@withContext saved
                }
            }
            val localProp = readLocalMarketProperties()
            if (localProp.isNotBlank() && !isRawModelOnly(localProp)) {
                cachedDeviceName = localProp
                saveToCache(context, localProp)
                return@withContext localProp
            }
        }

        val brand = Build.MANUFACTURER.ifBlank { Build.BRAND }
        val model = Build.MODEL
        val device = Build.DEVICE

        // Try Google Suggest
        var resolved = queryGoogleSuggest(brand, model, device)

        // Try Wikipedia if Google Suggest didn't find a marketing name
        if (resolved == null) {
            resolved = queryWikipedia(brand, model)
        }

        if (!resolved.isNullOrBlank() && !isRawModelOnly(resolved)) {
            cachedDeviceName = resolved
            saveToCache(context, resolved)
            Log.d(TAG, "Successfully resolved online device name: $resolved (model=$model)")
            return@withContext resolved
        }

        val fallback = getCachedOrLocalDeviceName(context)
        cachedDeviceName = fallback
        fallback
    }

    fun formatDisplayDevice(marketName: String): String {
        val model = Build.MODEL
        if (marketName.isBlank() || marketName.equals(model, ignoreCase = true)) {
            return "${Build.MANUFACTURER} $model"
        }
        return if (marketName.contains(model, ignoreCase = true)) {
            marketName
        } else {
            "$marketName ($model)"
        }
    }

    private fun readLocalMarketProperties(): String {
        val candidates = listOf(
            "ro.product.marketname",
            "ro.vendor.oplus.market.name",
            "ro.oplus.market.name",
            "ro.product.vendor.marketname",
            "ro.vivo.market.name",
            "ro.config.marketing_name",
            "ro.vendor.product.ztename",
            "bluetooth.device.default_name",
            "persist.sys.device_name",
            "ro.semc.product.name",
            "ro.product.model.market"
        )
        for (prop in candidates) {
            val v = getSystemProperty(prop)
            if (v.isNotBlank() && !v.equals(Build.UNKNOWN, ignoreCase = true) &&
                !v.equals("null", ignoreCase = true) && !isRawModelOnly(v)
            ) {
                return v
            }
        }
        return ""
    }

    private fun isRawModelOnly(name: String): Boolean {
        val trimmed = name.trim()
        val model = Build.MODEL.trim()
        if (trimmed.equals(model, ignoreCase = true)) return true
        if (trimmed.equals("${Build.MANUFACTURER} $model", ignoreCase = true)) return true
        if (trimmed.equals("${Build.BRAND} $model", ignoreCase = true)) return true
        // Only internal code without branding
        return false
    }

    private fun queryGoogleSuggest(brand: String, model: String, device: String): String? {
        val queries = mutableListOf<String>()
        queries.add("$brand $model")
        if (device.isNotBlank() && !device.equals(model, ignoreCase = true)) {
            queries.add("$brand $device")
            queries.add("$device $brand")
        }
        queries.add("$model model name")

        for (q in queries) {
            runCatching {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val url = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=$encoded")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val jsonArray = JSONArray(body)
                if (jsonArray.length() > 1) {
                    val suggestions = jsonArray.getJSONArray(1)
                    for (i in 0 until suggestions.length()) {
                        val s = suggestions.getString(i).lowercase(Locale.ROOT)
                        if (MARKETING_KEYWORDS.any { s.contains(it) }) {
                            val candidate = cleanCandidate(s, brand, model)
                            if (!candidate.isNullOrBlank()) {
                                return candidate
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun queryWikipedia(brand: String, model: String): String? {
        val queries = listOf("$brand $model", model)
        for (q in queries) {
            runCatching {
                val encoded = URLEncoder.encode(q, "UTF-8")
                val url = URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&format=json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    setRequestProperty("User-Agent", "SharkNeo/2.5.5 (https://github.com/CYB3R0ID694/Shark-Moto)")
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val root = JSONObject(body)
                val items = root.optJSONObject("query")?.optJSONArray("search") ?: return@runCatching
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val title = item.optString("title", "")
                    val rawSnippet = item.optString("snippet", "")
                    val snippet = rawSnippet.replace(Regex("<[^>]+>"), "")

                    if (!title.startsWith("List of", ignoreCase = true) && !title.startsWith("Category:", ignoreCase = true)) {
                        if (snippet.contains(model, ignoreCase = true) || title.contains(model, ignoreCase = true)) {
                            val candidate = cleanCandidate(title, brand, model)
                            if (!candidate.isNullOrBlank()) return candidate
                        }
                    }

                    if (snippet.contains(model, ignoreCase = true)) {
                        val extracted = extractFromWikiSnippet(snippet, model, brand)
                        if (!extracted.isNullOrBlank()) {
                            val candidate = cleanCandidate(extracted, brand, model)
                            if (!candidate.isNullOrBlank()) return candidate
                        }
                    }
                }
            }
        }
        return null
    }

    private fun extractFromWikiSnippet(snippet: String, model: String, brand: String): String? {
        val idx = snippet.indexOf(model, ignoreCase = true)
        if (idx == -1) return null
        val prefix = snippet.substring(0, idx)
        val brandRegex = Regex("(?i)($brand\\s+[A-Za-z0-9+\\s]{2,25})")
        val matches = brandRegex.findAll(prefix).toList()
        if (matches.isNotEmpty()) {
            val candidate = matches.last().value.trim()
            return candidate.split(Regex("[:;,]")).firstOrNull()?.trim()
        }
        val wordSeqRegex = Regex("([A-Z][a-zA-Z0-9+]*(?:\\s+[A-Z0-9][a-zA-Z0-9+]*){1,3})")
        val seqs = wordSeqRegex.findAll(prefix).toList()
        if (seqs.isNotEmpty()) {
            return seqs.last().value.trim()
        }
        return null
    }

    private fun cleanCandidate(raw: String, brand: String, model: String): String? {
        var s = NOISE_REGEX.replace(raw, " ")
        s = s.replace(Regex("(?i)\\b" + Regex.escape(model) + "\\b"), " ")
        s = s.replace(Regex("[\"()\\[\\]\\-:,]+"), " ")
        s = s.replace(Regex("\\s+"), " ").trim()

        if (s.length < 3) return null
        if (s.equals(brand, ignoreCase = true)) return null

        val words = s.split(" ").filter { it.isNotBlank() }
        if (words.isEmpty()) return null

        val formatted = words.joinToString(" ") { w ->
            if (w.length <= 4 && w.all { it.isLetter() && it.isUpperCase() }) {
                w
            } else {
                w.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }

        return if (!formatted.startsWith(brand, ignoreCase = true)) {
            "${brand.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} $formatted"
        } else {
            formatted
        }
    }

    private fun getSystemProperty(key: String): String = runCatching {
        val p = Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", key))
        val text = p.inputStream.bufferedReader().use { it.readText() }.trim()
        p.waitFor()
        text
    }.getOrDefault("")

    private fun saveToCache(context: Context?, name: String) {
        if (context == null) return
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_KEY_MARKET_NAME, name)
                .apply()
        }
    }
}
