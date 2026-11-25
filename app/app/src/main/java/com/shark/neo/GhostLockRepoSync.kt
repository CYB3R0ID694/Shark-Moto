package com.shark.neo

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Synchronizes and imports .conf kernel profiles directly from the official
 * YuKongA/ghostlock-app GitHub repository.
 */
object GhostLockRepoSync {
    private const val TAG = "GhostLockRepoSync"
    private const val RAW_BASE_URL =
        "https://raw.githubusercontent.com/YuKongA/ghostlock-app/main/app/src/main/assets/kernel_profiles"
    private const val INDEX_URL = "$RAW_BASE_URL/index.conf"
    private const val API_CONTENTS_URL =
        "https://api.github.com/repos/YuKongA/ghostlock-app/contents/app/src/main/assets/kernel_profiles"

    data class SyncSummary(
        val totalImported: Int,
        val currentDeviceMatched: Boolean,
        val currentDeviceModel: String? = null
    )

    /**
     * Attempts to fetch the .conf profile specifically matching the device's running kernel release.
     */
    suspend fun syncCurrentKernel(context: Context, release: String): Result<DynamicKernelProfile?> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (release.isBlank()) return@runCatching null

                // Try fetching directly: <release>.conf
                val safeRelease = release.trim()
                val encodedName = URLEncoder.encode("$safeRelease.conf", "UTF-8").replace("+", "%20")
                val targetUrl = "$RAW_BASE_URL/$encodedName"

                val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", "Shark-Neo-Sync")
                }

                try {
                    if (conn.responseCode == 200) {
                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                        val parseRes = GhostLockConfParser.parseString(text, "$safeRelease.conf")
                        if (parseRes is ParseResult.Success) {
                            GhostLockConfParser.saveRawConf(context, safeRelease, text)
                            DynamicProfileManager.addProfile(context, parseRes.profile)
                            Log.i(TAG, "Successfully synced GhostLock profile for active kernel: $safeRelease")
                            return@runCatching parseRes.profile
                        }
                    }
                } finally {
                    conn.disconnect()
                }

                // If direct fetch didn't match, search index.conf
                val indexProfiles = fetchIndexEntries()
                val matchedFile = indexProfiles[safeRelease]
                if (matchedFile != null) {
                    val fileUrl = "$RAW_BASE_URL/${URLEncoder.encode(matchedFile, "UTF-8").replace("+", "%20")}"
                    val fileConn = (URL(fileUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8000
                        readTimeout = 12000
                        setRequestProperty("User-Agent", "Shark-Neo-Sync")
                    }
                    try {
                        if (fileConn.responseCode == 200) {
                            val text = fileConn.inputStream.bufferedReader().use { it.readText() }
                            val parseRes = GhostLockConfParser.parseString(text, matchedFile)
                            if (parseRes is ParseResult.Success) {
                                GhostLockConfParser.saveRawConf(context, safeRelease, text)
                                DynamicProfileManager.addProfile(context, parseRes.profile)
                                return@runCatching parseRes.profile
                            }
                        }
                    } finally {
                        fileConn.disconnect()
                    }
                }

                null
            }
        }

    /**
     * Discovers and imports all available .conf profiles from the GhostLock repository.
     */
    suspend fun syncAllFromRepo(context: Context, currentKernel: String = ""): Result<SyncSummary> =
        withContext(Dispatchers.IO) {
            runCatching {
                var importedCount = 0
                var currentMatched = false
                var currentModel: String? = null

                // Step 1: Discover file list via GitHub API or index.conf fallback
                val confFiles = discoverConfFiles()
                Log.i(TAG, "Discovered ${confFiles.size} GhostLock profile files in repository")

                for (filename in confFiles) {
                    // Skip templates, includes, and index
                    if (filename.equals("index.conf", ignoreCase = true) ||
                        filename.contains("template", ignoreCase = true) ||
                        filename.startsWith("credential", ignoreCase = true) ||
                        filename.startsWith("kernelsnitch", ignoreCase = true) ||
                        filename.startsWith("execution", ignoreCase = true)
                    ) continue

                    val urlStr = "$RAW_BASE_URL/${URLEncoder.encode(filename, "UTF-8").replace("+", "%20")}"
                    val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 6000
                        readTimeout = 8000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                    }

                    try {
                        if (conn.responseCode == 200) {
                            val text = conn.inputStream.bufferedReader().use { it.readText() }
                            val parseRes = GhostLockConfParser.parseString(text, filename)
                            if (parseRes is ParseResult.Success) {
                                GhostLockConfParser.saveRawConf(context, parseRes.profile.release, text)
                                DynamicProfileManager.addProfile(context, parseRes.profile)
                                importedCount++

                                if (currentKernel.isNotBlank() && parseRes.profile.release == currentKernel) {
                                    currentMatched = true
                                    currentModel = parseRes.profile.model
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to download $filename: ${e.message}")
                    } finally {
                        conn.disconnect()
                    }
                }

                SyncSummary(
                    totalImported = importedCount,
                    currentDeviceMatched = currentMatched,
                    currentDeviceModel = currentModel
                )
            }
        }

    private fun discoverConfFiles(): Set<String> {
        val files = mutableSetOf<String>()

        // 1. First fetch index.conf via raw.githubusercontent.com (fast and not rate-limited by GitHub API)
        val indexMap = fetchIndexEntries()
        files.addAll(indexMap.values)

        // 2. Fallback to GitHub Contents API if index.conf returned empty
        if (files.isEmpty()) {
            runCatching {
                val conn = (URL(API_CONTENTS_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }
                try {
                    if (conn.responseCode == 200) {
                        val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                        val array = JSONArray(jsonStr)
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val name = item.optString("name", "")
                            if (name.endsWith(".conf", ignoreCase = true)) {
                                files.add(name)
                            }
                        }
                    }
                } finally {
                    conn.disconnect()
                }
            }.onFailure { Log.w(TAG, "GitHub API listing failed: ${it.message}") }
        }

        return files
    }

    private fun fetchIndexEntries(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        runCatching {
            val conn = (URL(INDEX_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            }
            try {
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    // Match { release = "...", file = "..." } or release = "..." / file = "..."
                    val entryRegex = Regex("""release\s*=\s*"([^"]+)"[\s,]+file\s*=\s*"([^"]+)"""")
                    for (m in entryRegex.findAll(text)) {
                        val rel = m.groupValues[1].trim()
                        val fil = m.groupValues[2].trim()
                        if (fil.endsWith(".conf", ignoreCase = true)) {
                            result[rel] = fil
                        }
                    }
                    // Also support file-only lines or include lines
                    val incRegex = Regex("""(?:include|file\s*=)\s*"([^"]+\.conf)"""")
                    for (m in incRegex.findAll(text)) {
                        val fil = m.groupValues[1].substringAfterLast('/').trim()
                        val rel = fil.removeSuffix(".conf")
                        if (!result.containsKey(rel)) {
                            result[rel] = fil
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
        }.onFailure { Log.w(TAG, "Failed to fetch index.conf: ${it.message}") }
        return result
    }
}
