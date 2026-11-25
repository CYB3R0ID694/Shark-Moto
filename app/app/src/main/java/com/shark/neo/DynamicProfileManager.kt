package com.shark.neo

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class DynamicKernelProfile(
    val release: String,
    val model: String,
    val engine: String,
    val timerSite: String,
    val timerLow21: String,
    val copyLow21: String,
    val selinuxState: String,
    val panicOnWarn: String,
    val rootUser: String,
    val initUserNs: String,
    val initUcounts: String,
    val initGroups: String,
    val siteMask: String = "0xffff"
) {
    fun toJsonString(): String {
        return org.json.JSONObject().apply {
            put("release", release)
            put("model", model)
            put("engine", engine)
            put("timer_site", timerSite)
            put("timer_low21", timerLow21)
            put("copy_low21", copyLow21)
            put("selinux_state", selinuxState)
            put("panic_on_warn", panicOnWarn)
            put("root_user", rootUser)
            put("init_user_ns", initUserNs)
            put("init_ucounts", initUcounts)
            put("init_groups", initGroups)
            put("site_mask", siteMask)
        }.toString(2)
    }
}

object DynamicProfileManager {
    private const val TAG = "DynamicProfileManager"
    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/CYB3R0ID694/Shark-Moto/beta/profiles.json"
    private const val FILE_NAME = "dynamic_profiles.json"

    private val profiles = java.util.concurrent.ConcurrentHashMap<String, DynamicKernelProfile>()

    fun init(context: Context) {
        runCatching {
            context.assets.open("profiles.json").bufferedReader().use {
                loadFromJson(it.readText())
            }
        }.onFailure { Log.w(TAG, "No bundled profiles.json: ${it.message}") }

        val local = File(context.filesDir, FILE_NAME)
        if (local.exists()) {
            runCatching {
                loadFromJson(local.readText())
            }.onFailure { Log.w(TAG, "Failed to load local profiles: ${it.message}") }
        }

        // Also reload any raw GhostLock .conf profiles saved in filesDir/kernel_profiles
        val confDir = File(context.filesDir, "kernel_profiles")
        if (confDir.exists() && confDir.isDirectory) {
            confDir.listFiles { f -> f.extension.equals("conf", ignoreCase = true) }?.forEach { confFile ->
                runCatching {
                    val res = GhostLockConfParser.parseString(confFile.readText(), confFile.name)
                    if (res is ParseResult.Success && !profiles.containsKey(res.profile.release)) {
                        profiles[res.profile.release] = res.profile
                    }
                }.onFailure { Log.w(TAG, "Failed to load local .conf ${confFile.name}: ${it.message}") }
            }
        }
    }

    data class ValidationResult(val isValid: Boolean, val error: String? = null)

    /**
     * GhostLock-inspired kernel geometry validator.
     * Enforces canonical 64-bit address ranges and bitmask sanity before attempting exploit execution,
     * protecting against null pointer dereferences and kernel panics from corrupt profiles.
     */
    fun validateProfile(profile: DynamicKernelProfile): ValidationResult {
        if (profile.release.isBlank() || !profile.release.contains("Linux version", ignoreCase = true)) {
            return ValidationResult(false, "Invalid kernel release banner: must contain 'Linux version'")
        }
        if (profile.engine.isBlank()) {
            return ValidationResult(false, "Engine cannot be blank")
        }

        fun parseHex(name: String, hex: String, minAddr: ULong = 0xffffff8000000000u): Pair<ULong, String?> {
            val clean = hex.removePrefix("0x").removePrefix("0X").trim()
            val value = clean.toULongOrNull(16) ?: return 0uL to "Field '$name' has invalid hex value: '$hex'"
            if (minAddr > 0u && value < minAddr) {
                return value to "Field '$name' (0x${value.toString(16)}) is outside canonical 64-bit kernel space (>= 0x${minAddr.toString(16)})"
            }
            return value to null
        }

        val (_, sErr) = parseHex("selinux_state", profile.selinuxState)
        if (sErr != null) return ValidationResult(false, sErr)

        val (_, uErr) = parseHex("init_user_ns", profile.initUserNs)
        if (uErr != null) return ValidationResult(false, uErr)

        val (_, rErr) = parseHex("root_user", profile.rootUser)
        if (rErr != null) return ValidationResult(false, rErr)

        val (_, ucErr) = parseHex("init_ucounts", profile.initUcounts)
        if (ucErr != null) return ValidationResult(false, ucErr)

        val (_, gErr) = parseHex("init_groups", profile.initGroups)
        if (gErr != null) return ValidationResult(false, gErr)

        if (profile.timerSite.isNotBlank() && profile.timerSite != "0x0" && profile.timerSite != "0") {
            val (timerSite, tErr) = parseHex("timer_site", profile.timerSite)
            if (tErr != null) return ValidationResult(false, tErr)

            val tLowClean = profile.timerLow21.removePrefix("0x").removePrefix("0X").trim()
            val tLow = tLowClean.toULongOrNull(16)
            if (tLow != null) {
                val expectedLow21 = timerSite and 0x1fffffu
                if (tLow != expectedLow21) {
                    return ValidationResult(false, "timer_low21 (0x${tLow.toString(16)}) does not match timer_site low-21 bits (0x${expectedLow21.toString(16)})")
                }
            }
        }

        return ValidationResult(true)
    }

    private fun loadFromJson(jsonStr: String): Int {
        val json = JSONObject(jsonStr)
        val arr = json.optJSONArray("profiles") ?: return 0
        var count = 0
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val release = obj.getString("release")
            val profile = DynamicKernelProfile(
                release = release,
                model = obj.optString("model", "Unknown Device"),
                engine = obj.getString("engine"),
                timerSite = obj.getString("timer_site"),
                timerLow21 = obj.getString("timer_low21"),
                copyLow21 = obj.getString("copy_low21"),
                selinuxState = obj.getString("selinux_state"),
                panicOnWarn = obj.getString("panic_on_warn"),
                rootUser = obj.getString("root_user"),
                initUserNs = obj.getString("init_user_ns"),
                initUcounts = obj.getString("init_ucounts"),
                initGroups = obj.getString("init_groups"),
                siteMask = obj.optString("site_mask", "0xffff")
            )
            val validation = validateProfile(profile)
            if (validation.isValid) {
                profiles[release] = profile
                count++
            } else {
                Log.w(TAG, "Skipping invalid profile '${profile.model}' ($release): ${validation.error}")
            }
        }
        return count
    }

    fun lookup(release: String): DynamicKernelProfile? = profiles[release]

    fun profileCount(): Int = profiles.size

    fun allProfiles(): List<DynamicKernelProfile> = profiles.values.toList()

    fun addProfile(context: Context, profile: DynamicKernelProfile) {
        val validation = validateProfile(profile)
        require(validation.isValid) { "Profile validation failed: ${validation.error}" }
        profiles[profile.release] = profile
        saveProfiles(context)
    }

    fun removeProfile(context: Context, release: String) {
        profiles.remove(release)
        saveProfiles(context)
    }

    private fun saveProfiles(context: Context) {
        runCatching {
            val json = JSONObject()
            json.put("version", 1)
            val arr = org.json.JSONArray()
            for (p in profiles.values) {
                val obj = JSONObject().apply {
                    put("release", p.release)
                    put("model", p.model)
                    put("engine", p.engine)
                    put("timer_site", p.timerSite)
                    put("timer_low21", p.timerLow21)
                    put("copy_low21", p.copyLow21)
                    put("selinux_state", p.selinuxState)
                    put("panic_on_warn", p.panicOnWarn)
                    put("root_user", p.rootUser)
                    put("init_user_ns", p.initUserNs)
                    put("init_ucounts", p.initUcounts)
                    put("init_groups", p.initGroups)
                    put("site_mask", p.siteMask)
                }
                arr.put(obj)
            }
            json.put("profiles", arr)
            val local = File(context.filesDir, FILE_NAME)
            local.writeText(json.toString(2))
        }.onFailure { Log.e(TAG, "Failed to save profiles: ${it.message}") }
    }

    suspend fun syncRemote(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(REMOTE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("User-Agent", "Shark-Moto-Sync")
            }
            try {
                if (conn.responseCode != 200) {
                    throw IllegalStateException("HTTP ${conn.responseCode} while fetching profiles")
                }
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val loaded = loadFromJson(text)
                val local = File(context.filesDir, FILE_NAME)
                local.writeText(text)
                loaded
            } finally {
                conn.disconnect()
            }
        }
    }

    suspend fun syncGhostLock(context: Context, currentKernel: String = ""): Result<GhostLockRepoSync.SyncSummary> =
        GhostLockRepoSync.syncAllFromRepo(context, currentKernel)
}
