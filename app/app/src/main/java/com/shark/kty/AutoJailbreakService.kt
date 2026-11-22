package com.shark.kty

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoJailbreakService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lastNotified = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        val prefs = getSharedPreferences("pyb110", MODE_PRIVATE)
        val lang = AppLanguage.fromCode(prefs.getString("app_language", "en"))
        val first = notification(AppStrings.autoJailbreakPreparing(lang))
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIF_ID, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, first)
        }
        if (!prefs.getBoolean("auto_jailbreak", false)) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val power = getSystemService(PowerManager::class.java)
        val wake = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "shark:auto-jailbreak")
        wake.acquire(10 * 60 * 1000L)
        scope.launch {
            try {
                runAuto()
            } catch (t: Throwable) {
                finishNotify("Auto jailbreak error: ${t.message}")
            } finally {
                runCatching { wake.release() }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun runAuto() {
        val prefs = getSharedPreferences("pyb110", MODE_PRIVATE)
        if (!prefs.getBoolean("auto_jailbreak", false)) return
        val lang = AppLanguage.fromCode(prefs.getString("app_language", "en"))

        /* A panic during the last auto attempt leaves the flag set; do not
         * keep bootlooping: drop the switch and say why. */
        if (prefs.getBoolean("auto_attempt", false)) {
            prefs.edit().putBoolean("auto_attempt", false).apply()
            val reason = getprop("ro.boot.bootreason")
            if (reason.contains("kernel_panic") || reason.contains("watchdog")) {
                prefs.edit().putBoolean("auto_jailbreak", false).apply()
                finishNotify(AppStrings.autoJailbreakPanicDisabled(lang))
                return
            }
        }

        updateNotification(AppStrings.autoJailbreakWaitingPriv(lang))
        var backend = false
        for (i in 0 until 30) {
            if (PrivService.pick() != null) {
                backend = true
                break
            }
            delay(2000)
        }
        if (!backend) {
            finishNotify(AppStrings.autoJailbreakPrivNotReady(lang))
            return
        }
        if (!PrivService.granted()) {
            finishNotify(AppStrings.autoJailbreakPrivNotGranted(lang))
            return
        }

        val release = runCatching { android.system.Os.uname().release }
            .getOrDefault(JailbreakCore.KERNEL_UNKNOWN)
        val kind = JailbreakCore.kindFor(release)
        if (kind == null) {
            finishNotify(AppStrings.autoJailbreakKernelUnsupported(release, lang))
            return
        }
        val active = runCatching { JailbreakCore.readDeviceState().first }.getOrDefault(false)
        if (active) {
            finishNotify(AppStrings.autoJailbreakAlreadyActive(lang))
            return
        }

        val managerKind = prefs.getString("manager", "KERNELSU") ?: "KERNELSU"
        var managerPkg = JailbreakCore.managerRuntime(managerKind).second
        if (managerKind == "NEXT" || managerKind == "NEXT_SPOOFED") {
            val savedSpoofed = prefs.getString("custom_spoofed_pkg", null)?.trim()
            if (!savedSpoofed.isNullOrEmpty() && runCatching { packageManager.getPackageInfo(savedSpoofed, 0) }.isSuccess) {
                managerPkg = savedSpoofed
            } else {
                val detected = JailbreakCore.findSpoofedNextPackage(this) { cmd ->
                    val p = PrivService.newProcess(arrayOf("sh", "-c", cmd), arrayOf("PATH=/system/bin:/system/xbin"), "/")
                    try {
                        val out = p.getInputStream().bufferedReader().use { it.readText() }
                        p.waitFor() to out
                    } finally {
                        p.close()
                    }
                }
                if (!detected.isNullOrEmpty()) {
                    managerPkg = detected
                }
            }
        }
        if (!managerPkg.matches(Regex("^[a-zA-Z0-9._]+$"))) {
            finishNotify("Invalid manager package name: $managerPkg")
            return
        }
        val preflight = runCatching {
            val p = PrivService.newProcess(
                arrayOf(
                    "sh", "-c",
                    "[ -n \"\$(pm path '$managerPkg' 2>/dev/null)\" ] || " +
                        "[ -x /data/adb/ksu/bin/ksud ] || [ -x /data/adb/ksud ]"
                ),
                arrayOf("PATH=/system/bin:/system/xbin:/data/adb/ksu/bin", "HOME=/"), "/"
            )
            try {
                p.waitFor()
            } finally {
                p.close()
            }
        }.getOrDefault(-1)
        if (preflight != 0) {
            finishNotify(AppStrings.autoJailbreakManagerNotInstalled(managerPkg, lang))
            return
        }
        if (!RunLock.tryAcquire()) {
            finishNotify(AppStrings.actionRunning(lang))
            return
        }
        try {
            prefs.edit().putBoolean("auto_attempt", true).apply()
            val nativeDir = applicationInfo.nativeLibraryDir
            val lib = JailbreakCore.runnerLib(nativeDir, kind, release)
            if (lib == null) {
                finishNotify(AppStrings.autoJailbreakKernelUnsupported(release, lang))
                return
            }
            val cfg = JailbreakCore.EnvConfig(
                kind = kind,
                release = release,
                managerKind = managerKind,
                antiRootGuard = prefs.getBoolean("antiroot_guard", true),
                unloadGuard = prefs.getBoolean("unload_guard", true),
                susfsSupport = prefs.getBoolean("susfs_support", false),
                vfsSupport = prefs.getBoolean("vfs_support", false),
                universal = managerKind == "DYNAMIC" && JailbreakCore.supportsUniversal(kind),
                chaseRetry = prefs.getBoolean("chase_retry", JailbreakCore.defaultChaseRetry(kind)),
                customManagerPkg = managerPkg,
                safeModeModules = prefs.getBoolean("safe_mode_modules", false)
            )
            val env = JailbreakCore.buildEnv(applicationContext, cfg, nativeDir)
            val model = JailbreakCore.resolveDeviceName()
            HistoryStore.start(
                applicationContext, JailbreakCore.managerLabel(managerKind), kind, release, model
            )
            updateNotification(AppStrings.autoJailbreakInProgress(lang))
            var lastLine = ""
            val startedAt = System.currentTimeMillis()
            val rc = JailbreakCore.runStreaming(arrayOf(lib, "run"), env, nativeDir) { line ->
                HistoryStore.append(line)
                lastLine = line
                updateNotification(line.take(90))
            }
            val ok = runCatching { JailbreakCore.readDeviceState().first }.getOrDefault(false)
            val success = rc == 0 && ok
            HistoryStore.finish(success, rc, System.currentTimeMillis() - startedAt)
            if (success) {
                finishNotify(AppStrings.autoJailbreakSuccess(lang))
            } else {
                val tail = if (lastLine.isNotBlank()) " ($lastLine)" else ""
                finishNotify(AppStrings.autoJailbreakFailed(rc, tail, lang))
            }
        } finally {
            prefs.edit().putBoolean("auto_attempt", false).apply()
            RunLock.release()
        }
    }

    private fun getprop(key: String): String = runCatching {
        val p = Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", key))
        val text = p.inputStream.bufferedReader().use { it.readText() }.trim()
        p.waitFor()
        text
    }.getOrDefault("")

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, "开机自动越狱", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "开机后自动执行越狱并显示结果"
                setShowBadge(false)
            }
        )
    }

    private fun notification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Shark Neo 自动越狱")
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(text: String) {
        val now = System.currentTimeMillis()
        if (now - lastNotified < 1500) return
        lastNotified = now
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, notification(text))
    }

    private fun finishNotify(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val done = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Shark Neo 自动越狱")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID, done)
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private companion object {
        const val CHANNEL_ID = "auto_jailbreak"
        const val NOTIF_ID = 98260
    }
}
