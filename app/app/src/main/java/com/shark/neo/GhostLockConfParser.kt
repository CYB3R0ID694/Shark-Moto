package com.shark.neo

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Native HOCON / .conf Parser for GhostLock (CVE-2026-43499) kernel profiles.
 * Allows importing .conf profiles directly into Shark-Neo's dynamic profile engine.
 */
object GhostLockConfParser {
    private const val TAG = "GhostLockConfParser"

    data class ParsedConf(
        val release: String,
        val model: String,
        val route: String,
        val symbols: Map<String, String>,
        val offsets: Map<String, String>,
        val rawText: String
    )

    suspend fun parseFromUri(context: Context, uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            } ?: return@withContext ParseResult.Error("Could not read .conf file.")
            val filename = uri.lastPathSegment ?: "profile.conf"
            val result = parseString(content, filename)

            // Persist raw .conf to kernel_profiles directory for native runner
            if (result is ParseResult.Success) {
                saveRawConf(context, result.profile.release, content)
            }
            result
        }.getOrElse {
            Log.e(TAG, "Failed to parse .conf from uri", it)
            ParseResult.Error("Failed to parse .conf: ${it.message}")
        }
    }

    fun parseString(text: String, filename: String = "profile.conf"): ParseResult {
        return runCatching {
            val parsed = parseHocon(text)
            if (parsed.release.isBlank()) {
                return ParseResult.Error("Missing 'release' field in .conf profile.")
            }

            val selinux = parsed.symbols["selinux_state"]
                ?: parsed.symbols["selinux_enforcing"]
                ?: "0x0"
            val initUserNs = parsed.symbols["init_user_ns"]
                ?: "0x0"
            val rootUser = parsed.symbols["init_cred"]
                ?: parsed.symbols["root_user"]
                ?: "0x0"
            val initUcounts = parsed.symbols["init_ucounts"] ?: "0x0"
            val initGroups = parsed.symbols["init_groups"] ?: "0x0"
            val panicOnWarn = parsed.symbols["panic_on_warn"] ?: "0x0"

            val displayModel = if (parsed.model.isNotBlank()) {
                "${parsed.model} (GhostLock .conf)"
            } else {
                "GhostLock Profile (${filename.removeSuffix(".conf")})"
            }

            val dynamicProfile = DynamicKernelProfile(
                release = parsed.release,
                model = displayModel,
                engine = "CVE43499_FUTEX",
                timerSite = "0x0",
                timerLow21 = "0x0",
                copyLow21 = "0x0",
                selinuxState = selinux,
                panicOnWarn = panicOnWarn,
                rootUser = rootUser,
                initUserNs = initUserNs,
                initUcounts = initUcounts,
                initGroups = initGroups,
                siteMask = if (parsed.route.isNotBlank()) parsed.route else "select_stack"
            )

            val details = buildString {
                appendLine("Target: ${parsed.release}")
                appendLine("Model: $displayModel")
                appendLine("Engine: CVE-2026-43499 (GhostLock rtmutex/futex)")
                appendLine("Route: ${dynamicProfile.siteMask}")
                appendLine("Symbols found: ${parsed.symbols.size}")
                appendLine("Offsets found: ${parsed.offsets.size}")
                if (selinux != "0x0") appendLine("selinux_state: $selinux")
                if (initUserNs != "0x0") appendLine("init_user_ns: $initUserNs")
                if (rootUser != "0x0") appendLine("init_cred: $rootUser")
            }

            ParseResult.Success(
                profile = dynamicProfile,
                banner = parsed.release,
                symbolsCount = parsed.symbols.size + parsed.offsets.size,
                details = details
            )
        }.getOrElse {
            Log.e(TAG, "Failed to parse HOCON", it)
            ParseResult.Error("Invalid GhostLock .conf format: ${it.message}")
        }
    }

    private fun parseHocon(text: String): ParsedConf {
        var release = ""
        var model = ""
        var route = "select_stack"
        val symbols = mutableMapOf<String, String>()
        val offsets = mutableMapOf<String, String>()

        var currentBlock: String? = null

        val lines = text.lines()
        for (rawLine in lines) {
            val line = stripComments(rawLine).trim()
            if (line.isEmpty()) continue

            if (line.endsWith("{")) {
                val blockName = line.substringBefore("{").trim().lowercase().replace(":", "").replace("=", "").trim()
                currentBlock = blockName
                continue
            }

            if (line == "}") {
                currentBlock = null
                continue
            }

            val sepIndex = when {
                line.contains("=") -> line.indexOf('=')
                line.contains(":") -> line.indexOf(':')
                else -> -1
            }

            if (sepIndex == -1) continue

            val key = line.substring(0, sepIndex).trim()
            val value = unquote(line.substring(sepIndex + 1).trim())

            when (currentBlock) {
                "symbols" -> symbols[key] = normalizeHex(value)
                "offsets" -> offsets[key] = normalizeHex(value)
                else -> {
                    when (key.lowercase()) {
                        "release" -> release = value
                        "model" -> model = value
                        "route" -> route = value
                        "selinux_state", "selinux" -> symbols["selinux_state"] = normalizeHex(value)
                        "init_user_ns" -> symbols["init_user_ns"] = normalizeHex(value)
                        "init_cred" -> symbols["init_cred"] = normalizeHex(value)
                    }
                }
            }
        }

        return ParsedConf(
            release = release,
            model = model,
            route = route,
            symbols = symbols,
            offsets = offsets,
            rawText = text
        )
    }

    private fun stripComments(line: String): String {
        var inQuotes = false
        val sb = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
                sb.append(c)
                i++
                continue
            }
            if (!inQuotes) {
                if (c == '#' || (c == '/' && i + 1 < line.length && line[i + 1] == '/')) {
                    break
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    private fun unquote(s: String): String {
        var str = s.trim()
        if (str.endsWith(",")) str = str.substring(0, str.length - 1).trim()
        if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
            str = str.substring(1, str.length - 1)
        }
        return str
    }

    private fun normalizeHex(s: String): String {
        val trimmed = unquote(s)
        if (trimmed.startsWith("0x", ignoreCase = true)) return trimmed.lowercase()
        val num = trimmed.toLongOrNull()
        if (num != null) return "0x%x".format(num)
        return trimmed
    }

    fun saveRawConf(context: Context, release: String, rawConf: String) {
        runCatching {
            val dir = File(context.filesDir, "kernel_profiles")
            if (!dir.exists()) dir.mkdirs()
            val safeName = release.replace('/', '_').replace('\\', '_')
            val file = File(dir, "$safeName.conf")
            file.writeText(rawConf)
            Log.i(TAG, "Saved raw GhostLock profile: ${file.absolutePath}")
        }.onFailure { Log.e(TAG, "Failed to save raw .conf: ${it.message}") }
    }
}
