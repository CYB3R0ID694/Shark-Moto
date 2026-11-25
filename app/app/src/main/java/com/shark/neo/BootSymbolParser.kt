package com.shark.neo

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

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
    private val SYMBOL_TYPES = "aAbBcCdDeEfFgGiInNpPrRsStTuUvVwW?-".toByteArray(Charsets.US_ASCII).toSet()

    suspend fun parseFromUri(context: Context, uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val filename = uri.lastPathSegment ?: "unknown"

            val inputStream = resolver.openInputStream(uri)
                ?: return@withContext ParseResult.Error("Could not read selected file.")

            // Inspect stream header without loading multi-gigabyte OTA ZIP into memory (GhostLock-inspired memory guard)
            val buffered = java.io.BufferedInputStream(inputStream, 65536)
            buffered.mark(8)
            val header = ByteArray(4)
            val readHeader = buffered.read(header, 0, 4)
            buffered.reset()

            val isZip = readHeader == 4 &&
                    header[0] == 0x50.toByte() && header[1] == 0x4b.toByte() &&
                    header[2] == 0x03.toByte() && header[3] == 0x04.toByte()

            if (isZip) {
                // Stream ZIP entries directly to avoid OutOfMemoryError on full OTA ZIPs
                var hasPayloadBin = false
                ZipInputStream(buffered).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        val lower = name.lowercase()
                        if (lower.endsWith("payload.bin")) {
                            hasPayloadBin = true
                        }
                        if (lower.endsWith("boot.img") ||
                            lower == "boot" ||
                            lower.endsWith("init_boot.img") ||
                            lower.endsWith("vendor_boot.img") ||
                            lower == "image" ||
                            lower.endsWith("kernel")) {
                            Log.i(TAG, "Streaming target partition from ZIP: $name")
                            val entryBytes = readBoundedStream(zis, 192 * 1024 * 1024)
                            return@withContext parseBytes(entryBytes, name)
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                if (hasPayloadBin) {
                    return@withContext ParseResult.Error(
                        "OTA archive contains payload.bin. Modern A/B updates store partitions inside payload.bin. " +
                        "Please extract boot.img or init_boot.img using payload-dumper, or select the boot partition directly."
                    )
                }
                return@withContext ParseResult.Error("No boot.img, init_boot.img, or kernel found in ZIP archive.")
            }

            // Not a ZIP archive: read bounded stream (max 256MB) to prevent OOM on non-kernel files
            val rawBytes = buffered.use { readBoundedStream(it, 256 * 1024 * 1024) }
            parseBytes(rawBytes, filename)
        }.getOrElse {
            Log.e(TAG, "Failed to parse from uri", it)
            ParseResult.Error("Parse failed: ${it.message}")
        }
    }

    private fun readBoundedStream(input: java.io.InputStream, maxBytes: Int): ByteArray {
        val bos = ByteArrayOutputStream()
        val buffer = ByteArray(32768)
        var total = 0
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            total += read
            if (total > maxBytes) {
                throw IllegalStateException("File or partition exceeds maximum allowed size (${maxBytes / (1024 * 1024)}MB).")
            }
            bos.write(buffer, 0, read)
        }
        return bos.toByteArray()
    }

    fun parseBytes(data: ByteArray, filename: String): ParseResult {
        if (data.size < 64) {
            return ParseResult.Error("File too small (${data.size} bytes).")
        }

        // Check if ZIP archive containing boot.img / init_boot.img
        if (data.size >= 4 && data[0] == 0x50.toByte() && data[1] == 0x4b.toByte() &&
            data[2] == 0x03.toByte() && data[3] == 0x04.toByte()) {
            runCatching {
                ZipInputStream(ByteArrayInputStream(data)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        val lower = name.lowercase()
                        if (lower.endsWith("boot.img") ||
                            lower == "boot" ||
                            lower.endsWith("init_boot.img") ||
                            lower.endsWith("vendor_boot.img") ||
                            lower == "image" ||
                            lower.endsWith("kernel")) {
                            return parseBytes(zis.readBytes(), name)
                        }
                        entry = zis.nextEntry
                    }
                }
            }
        }

        // Check if GhostLock .conf profile (HOCON format)
        val textHeader = runCatching { String(data.take(1024).toByteArray(), Charsets.UTF_8) }.getOrNull() ?: ""
        if (filename.endsWith(".conf", ignoreCase = true) || textHeader.contains("symbols {") || textHeader.contains("route =") || textHeader.contains("route:")) {
            val confResult = GhostLockConfParser.parseString(String(data, Charsets.UTF_8), filename)
            if (confResult is ParseResult.Success) {
                return confResult
            }
        }

        // Check if text symbol file (e.g. ic-syms.txt)
        if (textHeader.startsWith("#") || textHeader.contains("build=") || textHeader.contains("selinux_state=")) {
            return parseTextTable(String(data, Charsets.UTF_8), filename)
        }

        // Check container / extract kernel
        val kernelBytes: ByteArray = when {
            // Android Boot Image (v0 - v4)
            data.size >= 8 && data.take(8).toByteArray().contentEquals("ANDROID!".toByteArray(Charsets.US_ASCII)) -> {
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

        val detectedModel = detectDeviceName(data, decompressed, filename)

        val profile = DynamicKernelProfile(
            release = banner,
            model = "$detectedModel (Imported)",
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
            appendLine("timer_site: 0x%x (low21: 0x%x)".format(timerSite, timerLow21))
            appendLine("copy_low21: 0x%x".format(copyLow21))
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
        val headerVersion = if (data.size >= 44) buf.getInt(40) else 0
        val pageSize = if (headerVersion >= 3) 4096 else {
            val p = buf.getInt(36)
            if (p <= 0) 2048 else p
        }
        val kernelOffset = pageSize

        if (kernelOffset >= data.size) return null
        val available = data.size - kernelOffset
        val extractLen = if (kernelSize in 1..available) kernelSize.toInt() else available
        return data.copyOfRange(kernelOffset, kernelOffset + extractLen)
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
        var searchFrom = 0
        while (searchFrom < image.size) {
            val idx = indexOfBytes(image, needle, searchFrom)
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
            if (end > start) {
                val candidate = String(image, start, end - start, Charsets.US_ASCII).trim()
                if (candidate.isNotEmpty() && candidate[0].isDigit() && candidate.contains(".")) {
                    return candidate
                }
            }
            searchFrom = idx + 1
        }
        return null
    }

    private data class TokenTable(
        val tokens: List<ByteArray>,
        val tableOffset: Int,
        val indexOffset: Int
    )

    private fun findTokenTables(image: ByteArray): List<TokenTable> {
        val digitNeedle = ByteArray(20).apply {
            for (d in 0..9) {
                this[d * 2] = ('0'.code + d).toByte()
                this[d * 2 + 1] = 0
            }
        }
        val tables = mutableListOf<TokenTable>()
        var searchFrom = 0
        while (searchFrom < image.size) {
            val digitOffset = indexOfBytes(image, digitNeedle, searchFrom)
            if (digitOffset < 0) break

            val earliest = maxOf(0, digitOffset - '0'.code * (MAX_TOKEN_LEN + 1))
            var start = (earliest + ALIGN - 1) and -ALIGN
            while (start <= digitOffset) {
                val t = parseTokenTableAt(image, start, digitOffset)
                if (t != null) {
                    tables.add(t)
                }
                start += ALIGN
            }
            searchFrom = digitOffset + 1
        }
        return tables
    }

    private fun parseTokenTableAt(image: ByteArray, start: Int, digitOffset: Int): TokenTable? {
        if (start % ALIGN != 0) return null
        val tokens = ArrayList<ByteArray>(TOKEN_COUNT)
        val tokenOffsets = ArrayList<Int>(TOKEN_COUNT)
        val tokenStarts = ArrayList<Int>(TOKEN_COUNT)
        var pos = start

        for (i in 0 until TOKEN_COUNT) {
            tokenStarts.add(pos)
            val searchEnd = minOf(pos + MAX_TOKEN_LEN + 1, image.size)
            if (pos >= searchEnd) return null

            var zeroIdx = -1
            for (p in pos until searchEnd) {
                if (image[p] == 0.toByte()) {
                    zeroIdx = p
                    break
                }
            }
            if (zeroIdx == -1 || zeroIdx == pos) return null
            val token = image.copyOfRange(pos, zeroIdx)
            for (b in token) {
                val u = b.toInt() and 0xff
                if (u !in 0x20..0x7e) return null
            }
            tokenOffsets.add(pos - start)
            tokens.add(token)
            pos = zeroIdx + 1
        }

        if (tokenStarts['0'.code] != digitOffset) return null

        val checkChars = "0123456789_abcdefghijklmnopqrstuvwxyzT"
        for (ch in checkChars) {
            val tok = tokens[ch.code]
            if (tok.size != 1 || tok[0] != ch.code.toByte()) return null
        }

        val indexOffset = (pos + ALIGN - 1) and -ALIGN
        if (pos < indexOffset) {
            for (p in pos until indexOffset) {
                if (image[p] != 0.toByte()) return null
            }
        }

        val indexEnd = indexOffset + TOKEN_INDEX_SIZE
        if (indexEnd > image.size || tokenOffsets.last() > 0xFFFF) return null

        for (idx in 0 until TOKEN_COUNT) {
            val expected = tokenOffsets[idx]
            val actual = (image[indexOffset + idx * 2].toInt() and 0xff) or
                    ((image[indexOffset + idx * 2 + 1].toInt() and 0xff) shl 8)
            if (actual != expected) return null
        }

        return TokenTable(tokens, start, indexOffset)
    }

    private fun readMarkers(image: ByteArray, start: Int, limit: Int): List<Int> {
        if (start % ALIGN != 0 || start + 8 > limit) return emptyList()
        val first = readU32(image, start)
        val second = readU32(image, start + 4)
        if (first != 0L || second !in 0x200L..0x40000L) return emptyList()

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

    private fun parseNameSpans(
        image: ByteArray,
        namesOffset: Int,
        count: Int,
        markersOffset: Int,
        markers: List<Int>,
        uleb: Boolean
    ): List<Pair<Int, Int>>? {
        if (count == 0 || markers.size != (count + 255) / 256) return null
        val spans = ArrayList<Pair<Int, Int>>(count)
        var pos = namesOffset

        for (index in 0 until count) {
            if (index % 256 == 0 && (pos - namesOffset) != markers[index / 256]) {
                return null
            }
            if (pos >= markersOffset) return null

            var length = image[pos++].toInt() and 0xff
            if (uleb && (length and 0x80) != 0) {
                if (pos >= markersOffset) return null
                val high = image[pos++].toInt() and 0xff
                if ((high and 0x80) != 0) return null
                length = (length and 0x7f) or (high shl 7)
                pos++ // consumed high byte
            }
            if (length == 0 || length > 0x3fff || pos + length > markersOffset) return null
            spans.add(Pair(pos, length))
            pos += length
        }

        val aligned = (pos + ALIGN - 1) and -ALIGN
        if (aligned != markersOffset) return null
        for (p in pos until markersOffset) {
            if (image[p] != 0.toByte()) return null
        }
        return spans
    }

    private fun decodeNames(
        image: ByteArray,
        spans: List<Pair<Int, Int>>,
        tokens: List<ByteArray>
    ): List<Pair<Byte, String>>? {
        val decoded = ArrayList<Pair<Byte, String>>(spans.size)
        val nameSet = HashSet<String>(spans.size)

        for ((pos, length) in spans) {
            val out = ByteArrayOutputStream()
            for (p in pos until pos + length) {
                val tokIdx = image[p].toInt() and 0xff
                if (tokIdx >= tokens.size) return null
                out.write(tokens[tokIdx])
            }
            val expanded = out.toByteArray()
            if (expanded.isEmpty() || expanded.size > 4096) return null

            val kind = expanded[0]
            if (kind !in SYMBOL_TYPES) return null

            val nameBytes = expanded.copyOfRange(1, expanded.size)
            if (nameBytes.isEmpty()) return null

            for (b in nameBytes) {
                val u = b.toInt() and 0xff
                if (u !in 0x21..0x7e) return null
            }
            val name = String(nameBytes, Charsets.US_ASCII)
            decoded.add(Pair(kind, name))
            nameSet.add(name)
        }

        if ("_text" !in nameSet) return null
        return decoded
    }

    private data class RecoveredNameTable(
        val numSymsOffset: Int,
        val namesOffset: Int,
        val markersOffset: Int,
        val names: List<Pair<Byte, String>>
    )

    private fun findNameTables(
        image: ByteArray,
        tokens: List<ByteArray>,
        tokenTableOffset: Int
    ): List<RecoveredNameTable> {
        val lower = maxOf(0, tokenTableOffset - MARKER_SEARCH_WINDOW)
        val markerCandidates = mutableListOf<Pair<Int, List<Int>>>()
        var pos = (lower + ALIGN - 1) and -ALIGN
        while (pos + 8 <= tokenTableOffset) {
            val m = readMarkers(image, pos, tokenTableOffset)
            if (m.isNotEmpty()) {
                markerCandidates.add(Pair(pos, m))
            }
            pos += ALIGN
        }

        val recovered = mutableListOf<RecoveredNameTable>()
        for ((markersOffset, markers) in markerCandidates) {
            val minCount = (markers.size - 1) * 256 + 1
            val maxCount = markers.size * 256
            val approxNames = markersOffset - markers.last()
            if (approxNames < 0) continue

            val searchStart = maxOf(0, approxNames - NAME_TAIL_SEARCH)
            var nso = approxNames - (approxNames % ALIGN)
            while (true) {
                if (nso + 8 <= image.size) {
                    val count = readU32(image, nso).toInt()
                    val paddingZero = readU32(image, nso + 4) == 0L
                    if (count in minCount..maxCount && paddingZero) {
                        val namesOffset = nso + ALIGN
                        for (uleb in listOf(true, false)) {
                            val spans = parseNameSpans(image, namesOffset, count, markersOffset, markers, uleb)
                            if (spans != null) {
                                val decoded = decodeNames(image, spans, tokens)
                                if (decoded != null) {
                                    recovered.add(RecoveredNameTable(nso, namesOffset, markersOffset, decoded))
                                    break
                                }
                            }
                        }
                    }
                }
                if (nso < searchStart + ALIGN) break
                nso -= ALIGN
            }
        }
        return recovered
    }

    private fun isArm64KernelAddress(addr: Long): Boolean {
        return (addr ushr 48) == 0xffffL && (addr % 4096L == 0L)
    }

    private fun decodeAddresses(
        image: ByteArray,
        names: List<Pair<Byte, String>>,
        offsetsOffset: Int,
        relativeBaseOffset: Int
    ): Map<String, Long>? {
        val count = names.size
        val offsetsEnd = offsetsOffset + count * 4
        if (offsetsEnd > image.size) return null
        if (relativeBaseOffset < offsetsEnd || relativeBaseOffset + 8 > image.size) return null
        for (p in offsetsEnd until relativeBaseOffset) {
            if (image[p] != 0.toByte()) return null
        }

        val relativeBase = readU64(image, relativeBaseOffset)
        if (!isArm64KernelAddress(relativeBase)) return null

        val signedOffsets = IntArray(count)
        var negativeCount = 0
        for (i in 0 until count) {
            val off = readI32(image, offsetsOffset + i * 4)
            signedOffsets[i] = off
            if (off < 0) negativeCount++
        }

        val addresses = LongArray(count)
        if (negativeCount * 2 >= count) {
            for (i in 0 until count) {
                val off = signedOffsets[i]
                val addr = if (off < 0) {
                    relativeBase - 1L - off.toLong()
                } else {
                    off.toLong() and 0xFFFFFFFFL
                }
                addresses[i] = addr
            }
        } else {
            for (i in 0 until count) {
                val off = signedOffsets[i]
                addresses[i] = relativeBase + (off.toLong() and 0xFFFFFFFFL)
            }
        }

        // Monotonic check
        for (i in 0 until count - 1) {
            if (java.lang.Long.compareUnsigned(addresses[i], addresses[i + 1]) > 0) {
                return null
            }
        }

        var textAddr: Long? = null
        for (i in 0 until count) {
            if (names[i].second == "_text") {
                if (textAddr != null) return null
                textAddr = addresses[i]
            }
        }
        if (textAddr == null || textAddr != relativeBase) return null

        val syms = HashMap<String, Long>(count)
        for (i in 0 until count) {
            val name = names[i].second
            if (!syms.containsKey(name)) {
                syms[name] = addresses[i]
            }
        }
        return syms
    }

    private fun recoverKallsyms(image: ByteArray): Map<String, Long>? {
        val tokenTables = findTokenTables(image)
        Log.d(TAG, "Found ${tokenTables.size} token table candidate(s)")
        if (tokenTables.isEmpty()) return null

        for (table in tokenTables) {
            val nameTables = findNameTables(image, table.tokens, table.tableOffset)
            Log.d(TAG, "Found ${nameTables.size} name table candidate(s) for table at 0x${table.tableOffset.toString(16)}")
            for (nt in nameTables) {
                val count = nt.names.size
                val oldRelativeBase = nt.numSymsOffset - ALIGN
                val oldOffsets = oldRelativeBase - ((count * 4 + ALIGN - 1) and -ALIGN)
                val newOffsets = (table.indexOffset + TOKEN_INDEX_SIZE + ALIGN - 1) and -ALIGN
                val newRelativeBase = (newOffsets + count * 4 + ALIGN - 1) and -ALIGN

                // Try 6.4+ layout first
                var syms = decodeAddresses(image, nt.names, newOffsets, newRelativeBase)
                if (syms != null) {
                    Log.i(TAG, "Successfully decoded kallsyms using 6.4+ layout (${syms.size} symbols)")
                    return syms
                }

                // Try pre-6.4 layout
                syms = decodeAddresses(image, nt.names, oldOffsets, oldRelativeBase)
                if (syms != null) {
                    Log.i(TAG, "Successfully decoded kallsyms using pre-6.4 layout (${syms.size} symbols)")
                    return syms
                }
            }
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

    private fun findCallSiteInFunction(
        image: ByteArray,
        funcAddr: Long,
        baseAddr: Long,
        targetAddr: Long,
        maxInstructions: Int = 300
    ): Long? {
        val funcOffset = (funcAddr - baseAddr).toInt()
        if (funcOffset < 0 || funcOffset + maxInstructions * 4 > image.size) return null

        for (i in 0 until maxInstructions) {
            val pcOffset = funcOffset + i * 4
            val pc = baseAddr + pcOffset
            val insn = readI32(image, pcOffset)
            // BL instruction: opcode bits 31..26 are 100101 (0x25)
            if ((insn ushr 26) == 0x25) {
                var imm26 = insn and 0x03FFFFFF
                if ((imm26 and 0x02000000) != 0) {
                    imm26 = imm26 or (-1 shl 26)
                }
                val destAddr = pc + (imm26.toLong() * 4L)
                if (destAddr == targetAddr) {
                    return pc + 4L // Return address after BL
                }
            }
        }
        return null
    }

    private fun deriveEngineAndSites(banner: String, image: ByteArray, syms: Map<String, Long>): EngineSites {
        val base = syms["_text"] ?: 0L

        // Default engine selection
        val engine = when {
            banner.contains("6.6.118") -> "CVE66118"
            banner.contains("6.6.127") -> "CVE66127"
            banner.contains("6.6.89") || banner.startsWith("6.6.") -> "CVE6689"
            banner.contains("6.12.58") -> "CVE61258"
            banner.contains("6.12.69") -> "CVE61269"
            banner.contains("6.12.38") -> "CVE61238"
            banner.contains("6.12.30") -> "CVE61230"
            banner.contains("6.12.23") -> "CVE61223"
            banner.startsWith("6.18.") -> "CVE61821"
            banner.startsWith("6.1.") -> "CVE61"
            else -> "CVE66118"
        }

        // Baseline defaults
        var defaultTimerSite = 0xffffffc0801a4dfcUL.toLong()
        var defaultTimerLow21 = 0x1a4dfcL
        var defaultCopyLow21 = 0x0ab944L
        val siteMask = if (banner.startsWith("6.6.")) "0x1fffff" else "0xffff"

        when {
            banner.startsWith("6.1.") -> {
                defaultTimerSite = 0xffffffc008192b38UL.toLong()
                defaultTimerLow21 = 0x192b38L
                defaultCopyLow21 = 0xab948L
            }
            banner.startsWith("6.12.") -> {
                defaultTimerSite = 0xffffffc0801e1a14UL.toLong()
                defaultTimerLow21 = 0x1e1a14L
                defaultCopyLow21 = 0x0d1598L
            }
            banner.startsWith("6.18.") -> {
                defaultTimerSite = 0xffffffc080201a14UL.toLong()
                defaultTimerLow21 = 0x201a14L
                defaultCopyLow21 = 0x0e2598L
            }
            banner.startsWith("6.6.") -> {
                defaultTimerSite = 0xffffffc0801a4dfcUL.toLong()
                defaultTimerLow21 = 0x1a4dfcL
                defaultCopyLow21 = 0x0ab944L
            }
        }

        // Dynamic callsite discovery if uncompressed kernel image is available
        var dynTimerSite: Long? = null
        var dynCopyLow21: Long? = null

        if (image.isNotEmpty() && base != 0L) {
            val allocTargets = listOfNotNull(
                syms["kmem_cache_alloc"],
                syms["kmem_cache_alloc_node"],
                syms["kmem_cache_alloc_trace"],
                syms["kmem_cache_alloc_lru"]
            )
            val timerFuncs = listOfNotNull(
                syms["do_timer_create"],
                syms["__arm64_sys_timer_create"],
                syms["sys_timer_create"]
            )
            val copyFuncs = listOfNotNull(
                syms["copy_process"],
                syms["dup_task_struct"]
            )

            for (tFunc in timerFuncs) {
                for (aTarget in allocTargets) {
                    val site = findCallSiteInFunction(image, tFunc, base, aTarget)
                    if (site != null) {
                        dynTimerSite = site
                        break
                    }
                }
                if (dynTimerSite != null) break
            }

            for (cFunc in copyFuncs) {
                for (aTarget in allocTargets) {
                    val site = findCallSiteInFunction(image, cFunc, base, aTarget)
                    if (site != null) {
                        dynCopyLow21 = site and 0x1fffffL
                        break
                    }
                }
                if (dynCopyLow21 != null) break
            }
        }

        val timerSite = dynTimerSite ?: defaultTimerSite
        val timerLow21 = dynTimerSite?.let { it and 0x1fffffL } ?: defaultTimerLow21
        val copyLow21 = dynCopyLow21 ?: defaultCopyLow21

        return EngineSites(
            engine = engine,
            timerSite = timerSite,
            timerLow21 = timerLow21,
            copyLow21 = copyLow21,
            siteMask = siteMask
        )
    }

    private fun indexOfBytes(haystack: ByteArray, needle: ByteArray, fromIndex: Int = 0): Int {
        if (needle.isEmpty() || haystack.size < needle.size || fromIndex < 0) return -1
        val max = haystack.size - needle.size
        for (i in fromIndex..max) {
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

    fun detectDeviceName(data: ByteArray, decompressed: ByteArray, filename: String): String {
        // 1. Try DTB in boot.img or kernel
        val dtbModel = scanDeviceModelFromDtb(data) ?: scanDeviceModelFromDtb(decompressed)
        if (!dtbModel.isNullOrBlank()) {
            return dtbModel
        }

        // 2. Check if filename contains a device name (e.g. RMX3867_boot.img, realme_boot.img)
        val cleanName = filename.substringBeforeLast(".")
        val parts = cleanName.split("_", "-", " ").filter {
            it.isNotBlank() && !it.equals("boot", ignoreCase = true) &&
                    !it.equals("image", ignoreCase = true) && !it.equals("kernel", ignoreCase = true)
        }
        if (parts.isNotEmpty()) {
            return parts.joinToString(" ")
        }

        // 3. Fallback to host device info
        return "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    }

    private fun scanDeviceModelFromDtb(data: ByteArray): String? {
        val dtbMagic = byteArrayOf(0xd0.toByte(), 0x0d.toByte(), 0xfe.toByte(), 0xed.toByte())
        var searchFrom = 0
        while (searchFrom < data.size - 32) {
            val idx = indexOfBytes(data, dtbMagic, searchFrom)
            if (idx == -1) break

            val m = parseDtbModel(data, idx)
            if (!m.isNullOrBlank()) {
                return m
            }
            searchFrom = idx + 4
        }
        return null
    }

    private fun parseDtbModel(data: ByteArray, offset: Int): String? {
        if (offset + 32 > data.size) return null
        val magic = readU32BE(data, offset)
        if (magic != 0xd00dfeedL) return null

        val structOff = offset + readU32BE(data, offset + 8).toInt()
        val stringsOff = offset + readU32BE(data, offset + 12).toInt()
        if (structOff >= data.size || stringsOff >= data.size || structOff < offset || stringsOff < offset) return null

        var p = structOff
        if (p + 4 > data.size || readU32BE(data, p) != 1L) return null
        p += 4

        while (p < data.size && data[p] != 0.toByte()) {
            p++
        }
        p++
        p = (p + 3) and -4

        var modelVal: String? = null
        var compatVal: String? = null

        while (p + 8 <= data.size) {
            val tag = readU32BE(data, p)
            if (tag == 3L) {
                val propLen = readU32BE(data, p + 4).toInt()
                val nameOff = readU32BE(data, p + 8).toInt()
                p += 12

                if (propLen in 1..256 && p + propLen <= data.size && stringsOff + nameOff < data.size) {
                    val propName = readNullTerminatedString(data, stringsOff + nameOff)
                    if (propName == "model") {
                        val s = readNullTerminatedString(data, p, propLen)
                        if (s.isNotBlank() && !s.contains("generic", ignoreCase = true)) {
                            modelVal = s
                        }
                    } else if (propName == "compatible") {
                        val s = readNullTerminatedString(data, p, propLen)
                        if (s.isNotBlank() && !s.contains("generic", ignoreCase = true)) {
                            compatVal = s.replace(",", " ").trim()
                        }
                    }
                }
                p += (propLen + 3) and -4
            } else if (tag == 4L) {
                p += 4
            } else {
                break
            }
        }

        return modelVal ?: compatVal
    }

    private fun readU32BE(bytes: ByteArray, offset: Int): Long {
        if (offset + 4 > bytes.size || offset < 0) return 0L
        return ((bytes[offset].toLong() and 0xFF) shl 24) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 8) or
                (bytes[offset + 3].toLong() and 0xFF)
    }

    private fun readNullTerminatedString(bytes: ByteArray, offset: Int, maxLen: Int = 128): String {
        if (offset >= bytes.size || offset < 0) return ""
        var end = offset
        val limit = minOf(bytes.size, offset + maxLen)
        while (end < limit && bytes[end] != 0.toByte()) {
            end++
        }
        return String(bytes, offset, end - offset, Charsets.UTF_8).trim()
    }
}
