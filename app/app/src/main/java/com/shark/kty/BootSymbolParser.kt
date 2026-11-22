package com.shark.kty

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream

sealed class ParseResult {
    data class Success(
        val profile: DynamicKernelProfile,
        val banner: String,
        val symbolsCount: Int,
        val details: String
    ) : ParseResult()

    data class Error(val message: String) : ParseResult()
}

object BootSymbolParser {
    private const val TAG = "BootSymbolParser"
    private const val ALIGN = 8
    private const val TOKEN_COUNT = 256
    private const val TOKEN_INDEX_SIZE = TOKEN_COUNT * 2
    private const val MAX_TOKEN_LEN = 256
    private const val MARKER_SEARCH_WINDOW = 4 * 1024 * 1024
    private const val NAME_TAIL_SEARCH = 0x40000
    private const val MIN_MARKERS = 8
    private const val MAX_MARKERS = 4096

    suspend fun parseFromUri(context: Context, uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext ParseResult.Error("Could not read selected file.")
            parseBytes(bytes, uri.lastPathSegment ?: "unknown")
        }.getOrElse {
            Log.e(TAG, "Failed to parse from uri", it)
            ParseResult.Error("Parse failed: ${it.message}")
        }
    }

    fun parseBytes(data: ByteArray, filename: String): ParseResult {
        if (data.size < 64) {
            return ParseResult.Error("File too small ($data.size bytes).")
        }

        // Check if text symbol file (e.g. ic-syms.txt)
        val textHeader = runCatching { String(data.take(512).toByteArray(), Charsets.UTF_8) }.getOrNull() ?: ""
        if (textHeader.startsWith("#") || textHeader.contains("build=") || textHeader.contains("selinux_state=")) {
            return parseTextTable(String(data, Charsets.UTF_8), filename)
        }

        // Check container / extract kernel
        val kernelBytes: ByteArray = when {
            // Android Boot Image (v0 - v4)
            data.take(8).toByteArray().contentEquals("ANDROID!".toByteArray(Charsets.US_ASCII)) -> {
                extractFromBootImg(data) ?: return ParseResult.Error("Failed to extract kernel from boot.img header.")
            }
            else -> data
        }

        // Decompress kernel if compressed
        val decompressed: ByteArray = decompressKernel(kernelBytes)
            ?: return ParseResult.Error("Failed to decompress kernel image (unsupported compression format).")

        // Read banner
        val banner = scanKernelBanner(decompressed)
            ?: return ParseResult.Error("No 'Linux version' banner found in kernel image.")

        // Scan kallsyms
        val syms = recoverKallsyms(decompressed)
            ?: return ParseResult.Error("Could not locate kallsyms symbol table in kernel image.")

        val initUserNs = syms["init_user_ns"]
            ?: return ParseResult.Error("Required symbol 'init_user_ns' not found in kallsyms.")
        val rootUser = syms["root_user"]
            ?: (initUserNs + 0x290)
        val initUcounts = syms["init_ucounts"]
            ?: (initUserNs + 0x1B18)
        val initGroups = syms["init_groups"]
            ?: (initUserNs + 0x15B0)
        val selinuxState = syms["selinux_state"]
            ?: return ParseResult.Error("Required symbol 'selinux_state' not found in kallsyms.")
        val panicOnWarn = syms["panic_on_warn"] ?: 0L

        // Determine baseline architecture & engine
        val (engine, timerSite, timerLow21, copyLow21, siteMask) = deriveEngineAndSites(banner, decompressed, syms)

        val profile = DynamicKernelProfile(
            release = banner,
            model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL} (Imported)",
            engine = engine,
            timerSite = "0x%x".format(timerSite),
            timerLow21 = "0x%x".format(timerLow21),
            copyLow21 = "0x%x".format(copyLow21),
            selinuxState = "0x%x".format(selinuxState),
            panicOnWarn = "0x%x".format(panicOnWarn),
            rootUser = "0x%x".format(rootUser),
            initUserNs = "0x%x".format(initUserNs),
            initUcounts = "0x%x".format(initUcounts),
            initGroups = "0x%x".format(initGroups),
            siteMask = siteMask
        )

        val details = buildString {
            appendLine("Kernel: $banner")
            appendLine("Engine: $engine")
            appendLine("selinux_state: 0x%x".format(selinuxState))
            appendLine("init_user_ns: 0x%x".format(initUserNs))
            appendLine("panic_on_warn: 0x%x".format(panicOnWarn))
            appendLine("Total Symbols Recovered: ${syms.size}")
        }

        return ParseResult.Success(profile, banner, syms.size, details)
    }

    private fun extractFromBootImg(data: ByteArray): ByteArray? {
        val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val magic = ByteArray(8)
        buf.get(magic)
        if (!magic.contentEquals("ANDROID!".toByteArray(Charsets.US_ASCII))) return null

        val kernelSize = buf.getInt(8).toLong() and 0xFFFFFFFFL
        val headerVersion = buf.getInt(40)
        val pageSize = if (headerVersion >= 3) 4096 else buf.getInt(36).let { if (it <= 0) 2048 else it }
        val kernelOffset = pageSize

        if (kernelOffset + kernelSize > data.size) return null
        return data.copyOfRange(kernelOffset, (kernelOffset + kernelSize).toInt())
    }

    private fun decompressKernel(data: ByteArray): ByteArray? {
        if (data.size < 4) return null

        // LZ4 legacy frame: 0x02 0x21 0x4c 0x18
        if (data[0] == 0x02.toByte() && data[1] == 0x21.toByte() && data[2] == 0x4c.toByte() && data[3] == 0x18.toByte()) {
            return decodeLz4Legacy(data)
        }

        // Gzip: 0x1f 0x8b
        if (data[0] == 0x1f.toByte() && data[1] == 0x8b.toByte()) {
            return runCatching {
                GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }
            }.getOrNull()
        }

        // Raw ARM64 kernel (ARM\x64 at 0x38 or length > 1MB)
        if (data.size > 0x40 && data[0x38] == 'A'.code.toByte() && data[0x39] == 'R'.code.toByte() &&
            data[0x3a] == 'M'.code.toByte() && data[0x3b] == 0x64.toByte()) {
            return data
        }

        // Fallback: check if uncompressed string exists
        if (scanKernelBanner(data) != null) {
            return data
        }

        return null
    }

    private fun decodeLz4Legacy(src: ByteArray): ByteArray? {
        var i = 4
        val n = src.size
        var outBuf = ByteArray(maxOf(n * 3, 64 * 1024 * 1024))
        var outSize = 0

        fun ensureCapacity(needed: Int) {
            if (outSize + needed > outBuf.size) {
                var newCap = outBuf.size * 2
                while (outSize + needed > newCap) newCap *= 2
                outBuf = outBuf.copyOf(newCap)
            }
        }

        while (i + 4 <= n) {
            val bsz = (src[i].toInt() and 0xff) or
                    ((src[i + 1].toInt() and 0xff) shl 8) or
                    ((src[i + 2].toInt() and 0xff) shl 16) or
                    ((src[i + 3].toInt() and 0xff) shl 24)
            i += 4
            if (bsz == 0) break
            if (i + bsz > n) return null

            val blk = src
            var p = i
            val blkEnd = i + bsz

            while (p < blkEnd) {
                val token = blk[p++].toInt() and 0xff
                var ll = token ushr 4
                if (ll == 15) {
                    var s: Int
                    do {
                        if (p >= blkEnd) return null
                        s = blk[p++].toInt() and 0xff
                        ll += s
                    } while (s == 255)
                }
                if (p + ll > blkEnd) return null
                ensureCapacity(ll)
                System.arraycopy(blk, p, outBuf, outSize, ll)
                outSize += ll
                p += ll

                if (p >= blkEnd) break
                if (p + 2 > blkEnd) return null

                val offset = (blk[p].toInt() and 0xff) or ((blk[p + 1].toInt() and 0xff) shl 8)
                p += 2
                if (offset == 0) return null

                var ml = (token and 0x0f) + 4
                if ((token and 0x0f) == 15) {
                    var s: Int
                    do {
                        if (p >= blkEnd) return null
                        s = blk[p++].toInt() and 0xff
                        ml += s
                    } while (s == 255)
                }

                ensureCapacity(ml)
                val copyStart = outSize - offset
                if (copyStart < 0) return null
                for (m in 0 until ml) {
                    outBuf[outSize++] = outBuf[copyStart + (m % offset)]
                }
            }
            i += bsz
        }
        return outBuf.copyOf(outSize)
    }

    fun scanKernelBanner(image: ByteArray): String? {
        val needle = "Linux version ".toByteArray(Charsets.US_ASCII)
        val idx = indexOfBytes(image, needle)
        if (idx == -1) return null

        var start = idx + needle.size
        while (start < image.size && (image[start] == ' '.code.toByte() || image[start] == '\t'.code.toByte())) {
            start++
        }
        var end = start
        while (end < image.size && image[end] != 0.toByte() && image[end] != ' '.code.toByte() &&
            image[end] != '\r'.code.toByte() && image[end] != '\n'.code.toByte()) {
            end++
        }
        if (end <= start) return null
        return String(image, start, end - start, Charsets.US_ASCII).trim()
    }

    private fun recoverKallsyms(image: ByteArray): Map<String, Long>? {
        val digitNeedle = ByteArray(20).apply {
            for (d in 0..9) {
                this[d * 2] = ('0'.code + d).toByte()
                this[d * 2 + 1] = 0
            }
        }
        var digitOffset = indexOfBytes(image, digitNeedle)
        if (digitOffset < 0) return null

        // Locate token table
        val earliest = maxOf(0, digitOffset - '0'.code * (MAX_TOKEN_LEN + 1))
        var start = (earliest + ALIGN - 1) and -ALIGN
        var tokenTable: List<ByteArray>? = null
        var tokenTableOff = 0
        var indexOff = 0

        while (start <= digitOffset) {
            val res = parseTokenTableAt(image, start, digitOffset)
            if (res != null) {
                tokenTable = res.first
                tokenTableOff = start
                indexOff = res.second
                break
            }
            start += ALIGN
        }
        if (tokenTable == null) return null

        // Search for markers & names
        val lower = maxOf(0, tokenTableOff - MARKER_SEARCH_WINDOW)
        var pos = (lower + ALIGN - 1) and -ALIGN
        while (pos + 8 <= tokenTableOff) {
            val markers = readMarkers(image, pos, tokenTableOff)
            if (markers.size >= MIN_MARKERS) {
                val minCount = (markers.size - 1) * 256 + 1
                val maxCount = markers.size * 256
                val approxNames = pos - markers.last()
                if (approxNames >= 0) {
                    val searchStart = maxOf(0, approxNames - NAME_TAIL_SEARCH)
                    var nso = approxNames - (approxNames % ALIGN)
                    while (nso >= searchStart) {
                        if (nso + 8 <= image.size) {
                            val count = readU32(image, nso).toInt()
                            val zeroPad = readU32(image, nso + 4) == 0L
                            if (count in minCount..maxCount && zeroPad) {
                                val spans = parseNameSpans(image, nso + ALIGN, count, pos, markers)
                                if (spans != null) {
                                    val names = decodeNames(image, spans, tokenTable)
                                    if (names != null) {
                                        val syms = decodeAddresses(image, names, nso - ALIGN, tokenTableOff, indexOff)
                                        if (syms != null) return syms
                                    }
                                }
                            }
                        }
                        nso -= ALIGN
                    }
                }
            }
            pos += ALIGN
        }
        return null
    }

    private fun parseTokenTableAt(image: ByteArray, start: Int, digitOffset: Int): Pair<List<ByteArray>, Int>? {
        var pos = start
        val tokens = ArrayList<ByteArray>(TOKEN_COUNT)
        val tokenOffsets = ArrayList<Int>(TOKEN_COUNT)

        for (i in 0 until TOKEN_COUNT) {
            val searchEnd = minOf(pos + MAX_TOKEN_LEN + 1, image.size)
            var zeroIdx = -1
            for (p in pos until searchEnd) {
                if (image[p] == 0.toByte()) {
                    zeroIdx = p
                    break
                }
            }
            if (zeroIdx == -1 || zeroIdx == pos) return null
            val token = image.copyOfRange(pos, zeroIdx)
            tokenOffsets.add(pos - start)
            tokens.add(token)
            pos = zeroIdx + 1
        }

        val idxOff = (pos + ALIGN - 1) and -ALIGN
        if (idxOff + TOKEN_INDEX_SIZE > image.size) return null
        for (i in 0 until TOKEN_COUNT) {
            val off = (image[idxOff + i * 2].toInt() and 0xff) or ((image[idxOff + i * 2 + 1].toInt() and 0xff) shl 8)
            if (off != tokenOffsets[i]) return null
        }
        return Pair(tokens, idxOff)
    }

    private fun readMarkers(image: ByteArray, start: Int, limit: Int): List<Int> {
        if (start + 8 > limit) return emptyList()
        val first = readU32(image, start)
        val second = readU32(image, start + 4)
        if (first != 0L || second !in 0x200..0x40000) return emptyList()

        val markers = ArrayList<Int>()
        markers.add(first.toInt())
        markers.add(second.toInt())
        var p = start + 8
        while (markers.size < MAX_MARKERS && p + 4 <= limit) {
            val v = readU32(image, p).toInt()
            val delta = v - markers.last()
            if (delta !in 0x200..0x40000) break
            markers.add(v)
            p += 4
        }
        return if (markers.size >= MIN_MARKERS) markers else emptyList()
    }

    private fun parseNameSpans(image: ByteArray, namesOff: Int, count: Int, markersOff: Int, markers: List<Int>): List<Pair<Int, Int>>? {
        val spans = ArrayList<Pair<Int, Int>>(count)
        var pos = namesOff
        for (i in 0 until count) {
            if (i % 256 == 0 && (pos - namesOff) != markers[i / 256]) return null
            if (pos >= markersOff) return null
            val len = image[pos++].toInt() and 0xff
            if (len == 0 || pos + len > markersOff) return null
            spans.add(Pair(pos, len))
            pos += len
        }
        return spans
    }

    private fun decodeNames(image: ByteArray, spans: List<Pair<Int, Int>>, tokens: List<ByteArray>): List<Pair<Byte, String>>? {
        val names = ArrayList<Pair<Byte, String>>(spans.size)
        val needed = setOf("_text", "selinux_state", "init_user_ns")
        var matched = 0

        for ((pos, len) in spans) {
            val out = ByteArrayOutputStream()
            for (p in pos until pos + len) {
                out.write(tokens[image[p].toInt() and 0xff])
            }
            val expanded = out.toByteArray()
            if (expanded.isEmpty()) return null
            val kind = expanded[0]
            val name = String(expanded, 1, expanded.size - 1, Charsets.US_ASCII)
            names.add(Pair(kind, name))
            if (name in needed) matched++
        }
        return if (matched >= needed.size) names else null
    }

    private fun decodeAddresses(image: ByteArray, names: List<Pair<Byte, String>>, oldBaseOff: Int, tokenOff: Int, idxOff: Int): Map<String, Long>? {
        val count = names.size
        // Test pre-6.4 layout
        val oldOffsets = oldBaseOff - ((count * 4 + ALIGN - 1) and -ALIGN)
        val symsPre64 = decodeWithOffsets(image, names, oldOffsets, oldBaseOff)
        if (symsPre64 != null) return symsPre64

        // Test 6.4+ layout
        val newOffsets = (idxOff + TOKEN_INDEX_SIZE + ALIGN - 1) and -ALIGN
        val newBaseOff = (newOffsets + count * 4 + ALIGN - 1) and -ALIGN
        return decodeWithOffsets(image, names, newOffsets, newBaseOff)
    }

    private fun decodeWithOffsets(image: ByteArray, names: List<Pair<Byte, String>>, offsetsOff: Int, baseOff: Int): Map<String, Long>? {
        if (offsetsOff < 0 || baseOff + 8 > image.size) return null
        val base = readU64(image, baseOff)
        if ((base ushr 48) != 0xffffL) return null

        val count = names.size
        val result = HashMap<String, Long>(1024)
        for (i in 0 until count) {
            val off = readI32(image, offsetsOff + i * 4)
            val addr = if (off < 0) base - 1L - off else base + (off.toLong() and 0xFFFFFFFFL)
            result[names[i].second] = addr
        }

        if (result["_text"] == base && result.containsKey("selinux_state")) {
            return result
        }
        return null
    }

    private fun parseTextTable(text: String, filename: String): ParseResult {
        var banner = "Custom-Text-Profile"
        val syms = HashMap<String, Long>()

        text.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("# build=")) {
                banner = trimmed.removePrefix("# build=").trim()
            } else if (trimmed.contains("=")) {
                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) {
                    val k = parts[0].trim()
                    val v = parts[1].trim().removePrefix("0x").removePrefix("0X")
                    val addr = v.toLongOrNull(16)
                    if (addr != null) syms[k] = addr
                }
            }
        }

        val initUserNs = syms["init_user_ns"]
            ?: return ParseResult.Error("Text symbol table missing 'init_user_ns'.")
        val rootUser = syms["root_user"] ?: (initUserNs + 0x290)
        val initUcounts = syms["init_ucounts"] ?: (initUserNs + 0x1B18)
        val initGroups = syms["init_groups"] ?: (initUserNs + 0x15B0)
        val selinuxState = syms["selinux_state"]
            ?: return ParseResult.Error("Text symbol table missing 'selinux_state'.")
        val panicOnWarn = syms["panic_on_warn"] ?: 0L

        val (engine, timerSite, timerLow21, copyLow21, siteMask) = deriveEngineAndSites(banner, ByteArray(0), syms)

        val profile = DynamicKernelProfile(
            release = banner,
            model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL} ($filename)",
            engine = engine,
            timerSite = "0x%x".format(timerSite),
            timerLow21 = "0x%x".format(timerLow21),
            copyLow21 = "0x%x".format(copyLow21),
            selinuxState = "0x%x".format(selinuxState),
            panicOnWarn = "0x%x".format(panicOnWarn),
            rootUser = "0x%x".format(rootUser),
            initUserNs = "0x%x".format(initUserNs),
            initUcounts = "0x%x".format(initUcounts),
            initGroups = "0x%x".format(initGroups),
            siteMask = siteMask
        )

        return ParseResult.Success(profile, banner, syms.size, "Imported from $filename (${syms.size} symbols)")
    }

    private data class EngineSites(
        val engine: String,
        val timerSite: Long,
        val timerLow21: Long,
        val copyLow21: Long,
        val siteMask: String
    )

    private fun deriveEngineAndSites(banner: String, image: ByteArray, syms: Map<String, Long>): EngineSites {
        return when {
            banner.startsWith("6.1.") -> {
                // Linux 6.1 GKI
                EngineSites(
                    engine = "CVE61",
                    timerSite = 0xffffffc008192b38UL.toLong(),
                    timerLow21 = 0x192b38L,
                    copyLow21 = 0xab948L,
                    siteMask = "0xffff"
                )
            }
            banner.startsWith("6.6.") -> {
                // Linux 6.6 GKI
                EngineSites(
                    engine = "CVE6689",
                    timerSite = 0xffffffc080193b40UL.toLong(),
                    timerLow21 = 0x193b40L,
                    copyLow21 = 0x0aa5e0L,
                    siteMask = "0x1fffff"
                )
            }
            banner.startsWith("6.12.") -> {
                // Linux 6.12 GKI
                EngineSites(
                    engine = "CVE61230",
                    timerSite = 0xffffffc0801e1a14UL.toLong(),
                    timerLow21 = 0x1e1a14L,
                    copyLow21 = 0x0d1598L,
                    siteMask = "0xffff"
                )
            }
            banner.startsWith("6.18.") -> {
                // Linux 6.18
                EngineSites(
                    engine = "CVE61821",
                    timerSite = 0xffffffc080201a14UL.toLong(),
                    timerLow21 = 0x201a14L,
                    copyLow21 = 0x0e2598L,
                    siteMask = "0xffff"
                )
            }
            else -> {
                EngineSites(
                    engine = "CVE61",
                    timerSite = 0xffffffc008192b38UL.toLong(),
                    timerLow21 = 0x192b38L,
                    copyLow21 = 0xab948L,
                    siteMask = "0xffff"
                )
            }
        }
    }

    private fun indexOfBytes(haystack: ByteArray, needle: ByteArray): Int {
        if (needle.isEmpty() || haystack.size < needle.size) return -1
        val max = haystack.size - needle.size
        for (i in 0..max) {
            var found = true
            for (j in needle.indices) {
                if (haystack[i + j] != needle[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        return -1
    }

    private fun readU32(bytes: ByteArray, offset: Int): Long {
        if (offset + 4 > bytes.size) return 0L
        return (bytes[offset].toLong() and 0xFF) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 3].toLong() and 0xFF) shl 24)
    }

    private fun readI32(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun readU64(bytes: ByteArray, offset: Int): Long {
        if (offset + 8 > bytes.size) return 0L
        var res = 0L
        for (i in 0..7) {
            res = res or ((bytes[offset + i].toLong() and 0xFF) shl (i * 8))
        }
        return res
    }
}
