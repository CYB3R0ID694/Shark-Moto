package com.shark.kty

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import java.util.concurrent.atomic.AtomicBoolean

/** Shared between the UI flow and the boot auto-jailbreak service. */
object RunLock {
    private val running = AtomicBoolean(false)

    fun tryAcquire(): Boolean = running.compareAndSet(false, true)

    fun release() {
        running.set(false)
    }
}

object JailbreakCore {
    const val KERNEL_UNKNOWN = "未知"

    /* Exact uname -> exploit kind (name matches MainActivity.ExploitKind). */
    val PROFILE_BY_RELEASE: Map<String, String> = mapOf(
        "6.18.21-android17-5-gf957accaf563-ab15926176-4k" to "CVE61821",
        "6.18.21-android17-5-g7fcbffcad3e1-ab16353812-4k" to "CVE61821",
        "6.18.21-android17-5-g284297031c53-abogki555938958-4k" to "CVE61821",
        "6.18.21-android17-5-g3977fbeb3462-abogki563184321-4k" to "CVE61821",
        "6.18.21-android17-5-g1d099fcb35e0-abogki540753930-4k" to "CVE61821",
        "6.12.69-android16-6-g242e1a07f878-ab15865446-4k" to "CVE61269",
        "6.12.69-android16-6-g5fd39602326e-ab16372235-4k" to "CVE61269",
        "6.12.69-android16-6-g586bfab1b9c5-abogki536749445-4k" to "CVE61269",
        "6.12.38-android16-5-g844001fb8721-ab14552068-4k" to "CVE61238",
        "6.12.30-android16-5-g1ed949324a3e-ab13881345-4k" to "CVE61230",
        "6.12.58-android16-6-g7704a1ae279b-ab15213644-4k" to "CVE61258",
        "6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k" to "CVE61258",
        "6.12.58-android16-6-g09eda8c8505a-ab15950084-4k" to "CVE61258",
        "6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k" to "CVE61258",
        "6.12.58-android16-6-gdd6fd5c98918-abogki543627742-4k" to "CVE61258",
        "6.12.23-android16-5-g41f4cb1fa7c7-abogki465038634-4k" to "CVE61223",
        "6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k" to "CVE66118",
        "6.6.118-android15-8-gc4127a25dcf3-ab15863337-4k" to "CVE66118",
        "6.6.118-android15-8-gb1422e39c313-abogki552867473-4k" to "CVE66118",
        "6.6.127-android15-8-gd489d4ad2a70-abogki567717946-4k" to "CVE66127",
        "6.12.58-android16-6-g690108013dcc-abogki576129896-4k" to "CVE61258",
        "6.6.89-android15-8-gdcee9aa4fcbc-ab14676413-4k" to "CVE6689"
    )

    /* Devices whose support is only shipped in the full (vivo) build. */
    private val VIVO_IQOO_RELEASES = setOf(
        "6.12.23-android16-5-g41f4cb1fa7c7-abogki465038634-4k",
        "6.6.127-android15-8-gd489d4ad2a70-abogki567717946-4k",
        "6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k",
        "6.12.58-android16-6-gdd6fd5c98918-abogki543627742-4k",
        "6.12.58-android16-6-g690108013dcc-abogki576129896-4k",
        "6.18.21-android17-5-g3977fbeb3462-abogki563184321-4k"
    )

    private const val PD2606_RELEASE =
        "6.18.21-android17-5-g3977fbeb3462-abogki563184321-4k"

    fun kindFor(release: String): String? {
        if (!BuildConfig.VIVO_IQOO_SUPPORT && release in VIVO_IQOO_RELEASES)
            return null
        if (!BuildConfig.VIVO_61821_SUPPORT && release == PD2606_RELEASE)
            return null
        return PROFILE_BY_RELEASE[release] ?: DynamicProfileManager.lookup(release)?.engine
    }

    fun isPlantCore61821(release: String): Boolean =
        release == "6.18.21-android17-5-g284297031c53-abogki555938958-4k" ||
            release == "6.18.21-android17-5-g3977fbeb3462-abogki563184321-4k" ||
            release == "6.18.21-android17-5-g1d099fcb35e0-abogki540753930-4k"

    fun runnerLib(nativeDir: String, kind: String, release: String): String? = when (kind) {
        "CVE61821" -> if (isPlantCore61821(release)) {
            if (BuildConfig.VIVO_IQOO_SUPPORT && BuildConfig.VIVO_61821_SUPPORT)
                "$nativeDir/libk61821p.so"
            else "$nativeDir/libk61821x.so"
        } else "$nativeDir/libk61821.so"
        "CVE61269" -> "$nativeDir/libk61269.so"
        "CVE61238" -> "$nativeDir/libk61238.so"
        "CVE61230" -> "$nativeDir/libk61230.so"
        "CVE61258" -> if (BuildConfig.VIVO_IQOO_SUPPORT) "$nativeDir/libk61258.so"
        else "$nativeDir/libk61258x.so"
        "CVE61223" -> "$nativeDir/libk61223.so"
        "CVE66118" -> "$nativeDir/libk66118.so"
        "CVE66127" -> "$nativeDir/libk66127.so"
        "CVE6689" -> "$nativeDir/libk6689.so"
        else -> null
    }

    fun kmiFor(kind: String, release: String): String = when (kind) {
        "CVE6689", "CVE66118", "CVE66127" -> "android15-6.6"
        "CVE61821" -> "android17-6.18"
        else -> "android16-6.12"
    }

    fun isGateKernel(kind: String, release: String): Boolean =
        kind == "CVE66127" ||
            release == "6.18.21-android17-5-g3977fbeb3462-abogki563184321-4k" ||
            (kind == "CVE61258" &&
                (release.contains("gdd6fd5c98918") || release.contains("g690108013dcc")))

    fun supportsSusfs(kind: String, gate: Boolean): Boolean =
        !gate && kind != "CVE66118" && kind != "CVE66127" && kind != "CVE6689" && kind != "UNSUPPORTED"

    fun supportsUniversal(kind: String): Boolean =
        kind == "CVE61821" || kind == "CVE61269" || kind == "CVE61238" || kind == "CVE61230" ||
            kind == "CVE61258" || kind == "CVE61223"

    fun supportsChaseRetry(kind: String, release: String): Boolean =
        kind != "UNSUPPORTED" && !(kind == "CVE61821" && !isPlantCore61821(release))

    fun isVivoOemDevice(): Boolean =
        Build.MANUFACTURER.equals("vivo", ignoreCase = true) ||
            Build.MANUFACTURER.equals("iQOO", ignoreCase = true) ||
            Build.BRAND.equals("vivo", ignoreCase = true) ||
            Build.BRAND.equals("iQOO", ignoreCase = true)

    /** Chase pays off on vivo/iQOO; on Xiaomi and ColorOS ports it is opt-in. */
    fun defaultChaseRetry(kind: String): Boolean =
        isVivoOemDevice() || kind == "CVE61223" || kind == "CVE66127"

    /** ManagerKind name -> (PYB110_MANAGER value, package). */
    fun managerRuntime(kindName: String): Pair<String, String> = when (kindName) {
        "FINASU" -> "finasu" to "me.yuki.fina"
        "NEXT" -> "next" to "com.rifsxd.ksunext"
        "NEXT_SPOOFED" -> "next_spoofed" to "kqvdie.zeykvw.hpbcyd"
        "BAKASU" -> "bakasu" to "com.resukisu.resukisu"
        "SUKISU" -> "sukisu" to "com.sukisu.ultra"
        else -> "kernelsu" to "me.weishu.kernelsu"
    }

    fun managerLabel(kindName: String): String = when (kindName) {
        "FINASU" -> "FinaSU"
        "NEXT" -> "KernelSU Next"
        "NEXT_SPOOFED" -> "KernelSU Next (Spoofed)"
        "BAKASU" -> "BakaSU"
        "SUKISU" -> "SukiSU Ultra"
        "DYNAMIC" -> "Dynamic Switching"
        else -> "KernelSU"
    }

    /**
     * Attempts to dynamically discover an installed KernelSU / KernelSU-Next package
     * (including randomized/spoofed packages).
     */
    fun findSpoofedNextPackage(
        context: Context,
        runShellCmd: ((String) -> Pair<Int, String>)? = null
    ): String? {
        val pm = context.packageManager

        // 1. Check known default package names first
        for (pkg in listOf("com.rifsxd.ksunext", "kqvdie.zeykvw.hpbcyd")) {
            if (runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess) {
                return pkg
            }
        }

        // 2. Query via Intent for apps registering FlashModule (.zip viewer)
        runCatching {
            val zipIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("content://dummy/module.zip"), "application/zip")
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(zipIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(zipIntent, 0)
            }
            for (resolve in matches) {
                val pkg = resolve.activityInfo?.packageName ?: continue
                if (pkg == context.packageName) continue
                val label = runCatching { resolve.loadLabel(pm).toString() }.getOrDefault("")
                val act = resolve.activityInfo?.name.orEmpty()
                if (label.contains("KernelSU", ignoreCase = true) ||
                    act.endsWith(".FlashModule") ||
                    act.contains(".ui.MainActivity")
                ) {
                    return pkg
                }
            }
        }

        // 3. Fallback: Privileged shell scan for libksud.so among third-party packages
        if (runShellCmd != null) {
            runCatching {
                val script = "for p in \$(pm list packages -3 2>/dev/null | cut -d: -f2); do " +
                    "dir=\$(pm path \"\$p\" 2>/dev/null | head -n1 | sed -e 's/^package://' -e 's|/[^/]*$||'); " +
                    "if [ -f \"\$dir/lib/arm64/libksud.so\" ]; then echo \"\$p\"; break; fi; done"
                val (rc, out) = runShellCmd(script)
                val detected = out.lines().firstOrNull { it.isNotBlank() && !it.contains(" ") }?.trim()
                if (rc == 0 && !detected.isNullOrEmpty()) {
                    return detected
                }
            }
        }

        return null
    }

    fun resolveDeviceName(): String {
        val marketName = when (Build.MANUFACTURER.orEmpty().lowercase()) {
            "xiaomi" -> getSystemProperty("ro.product.marketname")
            "oppo", "oneplus", "realme", "oplus" -> getSystemProperty("ro.vendor.oplus.market.name")
            "vivo" -> getSystemProperty("ro.vivo.market.name")
            "honor", "huawei" -> getSystemProperty("ro.config.marketing_name")
            "zte", "nubia" -> getSystemProperty("ro.vendor.product.ztename")
            else -> ""
        }.trim().takeIf {
            it.isNotEmpty() && !it.equals(Build.UNKNOWN, ignoreCase = true) &&
                !it.equals("null", ignoreCase = true)
        }
        if (marketName != null) return marketName
        return buildString {
            append(Build.MANUFACTURER)
            if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) append(" ").append(Build.BRAND)
            append(" ").append(Build.MODEL)
        }
    }

    private fun getSystemProperty(key: String): String = runCatching {
        val p = Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", key))
        val text = p.inputStream.bufferedReader().use { it.readText() }.trim()
        p.waitFor()
        text
    }.getOrDefault("")

    data class EnvConfig(
        val kind: String,
        val release: String,
        val managerKind: String,
        val antiRootGuard: Boolean,
        val unloadGuard: Boolean,
        val susfsSupport: Boolean,
        val vfsSupport: Boolean,
        val universal: Boolean,
        val chaseRetry: Boolean,
        val customManagerPkg: String? = null,
        val safeModeModules: Boolean = false
    )

    fun buildEnv(context: Context, cfg: EnvConfig, dir: String): Array<String> {
        val runtime = managerRuntime(cfg.managerKind)
        val managerPkg = cfg.customManagerPkg?.takeIf { it.isNotBlank() } ?: runtime.second
        val gate = isGateKernel(cfg.kind, cfg.release)
        val susfsActive = cfg.susfsSupport && supportsSusfs(cfg.kind, gate)
        val susfsKo =
            if (cfg.kind == "CVE61821") "libsusfs_guard_lkm.so" else "libsusfs_guard_lkm_612.so"
        val nativeDir = context.applicationInfo.nativeLibraryDir
        return arrayOf(
            "PATH=/system/bin:/system/xbin:/data/adb/ksu/bin",
            "HOME=/",
            "PYB110_DIR=$dir",
            "PYB110_KMI=${kmiFor(cfg.kind, cfg.release)}",
            "PYB110_MANAGER=${runtime.first}",
            "PYB110_MANAGER_PKG=$managerPkg",
            "PYB110_LATE_LOAD=1",
            "PYB110_UNLOAD_GUARD=${if (cfg.unloadGuard && !Build.MANUFACTURER.equals("motorola", ignoreCase = true)) "1" else "0"}",
            "PYB110_GATE=${if (gate) "1" else "0"}",
            "PYB110_DISABLE_ANTIROOT=${if (cfg.antiRootGuard && !Build.MANUFACTURER.equals("motorola", ignoreCase = true)) "1" else "0"}",
            "PYB110_SUSFS=${if (susfsActive) "1" else "0"}",
            "PYB110_VFS=${if (cfg.vfsSupport) "1" else "0"}",
            "PYB110_CHASE=${if (cfg.chaseRetry) "1" else "0"}",
            "PYB110_UNIVERSAL=${if (cfg.universal) "1" else "0"}",
            "PYB110_UNIVERSAL_KSUD=$nativeDir/libksud_universal.so",
            "PYB110_FILES_DIR=${context.filesDir.absolutePath}",
            "PYB110_SUSFS_KO=$nativeDir/$susfsKo",
            "PYB110_VFS_KO=$nativeDir/libhybridmount.so",
            "PYB110_SAFE_MODE=${if (cfg.safeModeModules) "1" else "0"}"
        )
    }

    fun setKernelSuSafeMode(enabled: Boolean, runShell: (String) -> Pair<Int, String>): Pair<Int, String> {
        val cmd = if (enabled) {
            "mkdir -p /data/adb/ksu && touch /data/adb/ksu/disable"
        } else {
            "rm -f /data/adb/ksu/disable"
        }
        return runShell(cmd)
    }

    fun setSELinuxMode(enforce: Boolean, runShell: (String) -> Pair<Int, String>): Pair<Int, String> {
        val cmd = if (enforce) "setenforce 1" else "setenforce 0"
        return runShell(cmd)
    }

    fun getSELinuxMode(runShell: (String) -> Pair<Int, String>): String {
        val (rc, out) = runShell("getenforce")
        return if (rc == 0) out.trim() else "Unknown"
    }

    fun runStreaming(
        cmd: Array<String>,
        env: Array<String>,
        dir: String,
        onLine: (String) -> Unit
    ): Int {
        val p = PrivService.newProcess(cmd, env, dir)
        return try {
            val out = Thread {
                runCatching { p.getInputStream().bufferedReader().forEachLine(onLine) }
            }.apply { isDaemon = true; start() }
            val err = Thread {
                runCatching { p.getErrorStream().bufferedReader().forEachLine(onLine) }
            }.apply { isDaemon = true; start() }
            val rc = p.waitFor()
            out.join(2000)
            err.join(2000)
            rc
        } finally {
            p.close()
        }
    }

    fun readDeviceState(): Pair<Boolean, String> {
        val p = PrivService.newProcess(
            arrayOf(
                "sh", "-c",
                "if grep -q '^kernelsu .* Live ' /proc/modules; then echo YES; " +
                    "elif timeout 3 su -c id 2>/dev/null | grep -q uid=0; then echo YES; " +
                    "else echo NO; fi; getenforce"
            ),
            arrayOf("PATH=/system/bin:/system/xbin:/data/adb/ksu/bin", "HOME=/"), "/"
        )
        return try {
            val lines = p.getInputStream().bufferedReader().readLines()
            p.waitFor()
            val active = lines.getOrNull(0)?.trim() == "YES"
            active to (lines.getOrNull(1)?.trim().orEmpty())
        } finally {
            p.close()
        }
    }
}
