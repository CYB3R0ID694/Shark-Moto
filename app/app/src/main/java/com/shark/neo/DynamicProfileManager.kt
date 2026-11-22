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
    }

    private fun loadFromJson(jsonStr: String): Int {
        val json = JSONObject(jsonStr)
        val arr = json.optJSONArray("profiles") ?: return 0
        var count = 0
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val release = obj.getString("release")
            profiles[release] = DynamicKernelProfile(
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
            count++
        }
        return count
    }

    fun lookup(release: String): DynamicKernelProfile? = profiles[release]

    fun profileCount(): Int = profiles.size

    fun allProfiles(): List<DynamicKernelProfile> = profiles.values.toList()

    fun addProfile(context: Context, profile: DynamicKernelProfile) {
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
}
