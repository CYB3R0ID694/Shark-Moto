package com.shark.neo

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import rikka.shizuku.Shizuku
import roro.stellar.Stellar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notes
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.Dialog
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.effect.couiNavigationFeedback
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.Scaffold
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.shape.CouiShape
import com.itos.coui.switch.Switch
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.TopAppBarButton

private const val PROJECT_URL = "https://github.com/CYB3R0ID694/Shark-Moto"
private const val TELEGRAM_GROUP_URL = "https://t.me/sharkmotosupport"

private data class LicenseText(
    val name: String,
    val url: String?,
    val content: String? = null,
    val asset: String? = null,
)

private data class LicenseItem(
    val name: String,
    val version: String? = null,
    val author: String? = null,
    val website: String? = null,
    val licenses: List<LicenseText> = emptyList(),
)

private data class LocalLicense(
    val title: String,
    val summary: String,
    val licenseName: String,
    val asset: String,
    val url: String?,
)

private val localLicenses = listOf(
    LocalLicense(
        "Shark (This Project)",
        "Project-authored code",
        "GPL-3.0",
        "licenses/GPL-3.0.txt",
        "https://www.gnu.org/licenses/gpl-3.0.html"
    ),
    LocalLicense(
        "NOTICE",
        "Third-party sources, attribution, and notices",
        "NOTICE",
        "licenses/NOTICE.txt",
        null
    ),
    LocalLicense(
        "SUSFS guard / Hybrid Mount",
        "Kernel modules bundled with the app",
        "GPL-2.0",
        "licenses/GPL-2.0.txt",
        "https://www.gnu.org/licenses/old-licenses/gpl-2.0.html"
    ),
    LocalLicense(
        "AzirX1337 CVE-2026-98260",
        "MIT-licensed reference implementation",
        "MIT",
        "licenses/MIT.txt",
        "https://github.com/AzirX1337/CVE-2026-98260-OnePlus16-PYB110-Jailbreak"
    ),
    LocalLicense(
        "YuKongA CVE-2026-98260",
        "MIT 许可的参考实现",
        "MIT",
        "licenses/MIT.txt",
        "https://github.com/YuKongA/CVE-2026-98260"
    )
)

private fun Library.toLicenseItem(): LicenseItem = LicenseItem(
    name = name,
    version = artifactVersion,
    author = developers.joinToString(", ") { it.name.orEmpty() }
        .ifBlank { organization?.name.orEmpty() },
    website = website,
    licenses = licenses.map { LicenseText(it.name, it.url, it.licenseContent) },
)

private enum class AboutPage { HOME, LICENSES }

class MainActivity : ComponentActivity() {
    private val logs = mutableStateListOf<String>()
    private val timeFmt = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
    private var busy by mutableStateOf(false)
    private var authorized by mutableStateOf(false)
    private var binderReady by mutableStateOf(false)
    private var ksuActive by mutableStateOf(false)
    private var exploit by mutableStateOf(ExploitKind.UNSUPPORTED)
    private var manager by mutableStateOf(ManagerKind.KERNELSU)
    private var kernelVersion by mutableStateOf("Unknown")
    private var deviceModel by mutableStateOf("Unknown")
    private var selinuxText by mutableStateOf("Unknown")
    private var antiRootGuard by mutableStateOf(true)
    private var softRebootAfter by mutableStateOf(false)
    private var softRebootDelayMin by mutableStateOf(0)
    private var softRebootJob: Job? = null
    private val softRebootDelays = listOf(0, 1, 2, 3)
    private var unloadGuard by mutableStateOf(true)
    private var susfsSupport by mutableStateOf(false)
    private var vfsSupport by mutableStateOf(false)
    private var chaseRetry by mutableStateOf(true)
    private var autoJailbreak by mutableStateOf(false)
    private var dynamicSwitching by mutableStateOf(false)
    private var universalLoaded by mutableStateOf(false)
    private var hotManager by mutableStateOf(ManagerKind.KERNELSU)
    private var customSpoofedPkg by mutableStateOf("")
    private var hotSwitchBusy by mutableStateOf(false)
    private var hotSwitchError by mutableStateOf<String?>(null)
    private var tab by mutableStateOf(AppTab.HOME)
    private var showManual by mutableStateOf(false)
    private var showExtras by mutableStateOf(false)
    private var showDiagnostic by mutableStateOf(false)
    private var diagnosticRunning by mutableStateOf(false)
    private var diagnosticOutput by mutableStateOf("")
    private var betaChannel by mutableStateOf(false)
    private var syncingProfiles by mutableStateOf(false)
    private var syncingGhostLock by mutableStateOf(false)
    private var dynamicProfileCount by mutableStateOf(0)
    private var parsingBoot by mutableStateOf(false)
    private var parseResultDialog by mutableStateOf<ParseResult?>(null)
    private var parsedDeviceModel by mutableStateOf("")
    private var softRebootDialogVisible by mutableStateOf(false)
    private var softRebooting by mutableStateOf(false)
    private var rerunBlockedDialogVisible by mutableStateOf(false)
    private var bootSessionRan by mutableStateOf(false)
    private var bypassRebootGuard by mutableStateOf(false)
    private var uploadingReport by mutableStateOf(false)
    private var blockOtaUpdates by mutableStateOf(false)
    private var patchLevelDialogVisible by mutableStateOf(false)
    private var patchWarningDismissed by mutableStateOf(false)

    private val bootPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            parseBootUri(uri)
        }
    }
    private var legalAccepted by mutableStateOf(false)
    private var languageChosen by mutableStateOf(false)
    private var appLanguage by mutableStateOf(AppLanguage.EN)
    private var showLanguageDialog by mutableStateOf(false)
    private var amoledBlack by mutableStateOf(false)
    private var safeModeModules by mutableStateOf(false)
    private var selinuxPermissive by mutableStateOf(false)
    private var historyEntries by mutableStateOf(listOf<HistoryEntry>())
    private var historyDetail by mutableStateOf<HistoryEntry?>(null)
    private var historyDetailText by mutableStateOf("")
    private var pendingUpdate by mutableStateOf<UpdateInfo?>(null)
    private var updateDialogVisible by mutableStateOf(false)
    private var updateDownloading by mutableStateOf(false)
    private var updateProgress by mutableStateOf(0f)
    private var updateError by mutableStateOf<String?>(null)
    private var pendingApk by mutableStateOf<File?>(null)
    private var updateCheckedThisForeground = false
    private var droidspacesDialogVisible by mutableStateOf(false)
    private var droidspacesLoading by mutableStateOf(false)
    private var droidspacesRelease by mutableStateOf<DroidspacesRelease?>(null)
    private var droidspacesDownloading by mutableStateOf(false)
    private var droidspacesProgress by mutableStateOf(0f)
    private var droidspacesError by mutableStateOf<String?>(null)
    private var droidspacesSavedPath by mutableStateOf<String?>(null)

    /* UI-only state hoisted for the COUI top bar / dialogs. */
    private var aboutPage by mutableStateOf(AboutPage.HOME)
    private var aboutLicense by mutableStateOf<LicenseItem?>(null)
    private var showDonate by mutableStateOf(false)

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { code, result ->
        if (code == REQ_SHIZUKU) onPermissionResult(result == PackageManager.PERMISSION_GRANTED)
    }
    private val stellarPermissionListener = Stellar.OnRequestPermissionResultListener { code, allowed, _ ->
        if (code == REQ_SHIZUKU) onPermissionResult(allowed)
    }
    private val binderListener = Shizuku.OnBinderReceivedListener {
        binderReady = PrivService.pick() != null
        refreshAuth()
        refreshKsuAsync()
    }
    private val stellarBinderListener = Stellar.OnBinderReceivedListener {
        binderReady = PrivService.pick() != null
        refreshAuth()
        refreshKsuAsync()
    }

    private fun onPermissionResult(ok: Boolean) {
        authorized = ok
        addLog(if (ok) "${PrivService.pick()?.label ?: "Backend"} authorized" else "permission denied")
        if (ok) refreshKsuAsync()
    }

    override fun onStart() {
        super.onStart()
        if (updateCheckedThisForeground) return
        updateCheckedThisForeground = true
        if (updateDialogVisible || updateDownloading) return
        lifecycleScope.launch {
            when (val result = runCatching {
                UpdateChecker.check(applicationContext)
            }.getOrNull()) {
                is UpdateCheckResult.Available -> {
                    if (!updateDialogVisible) {
                        pendingUpdate = result.info
                        pendingApk = null
                        updateError = null
                        updateDialogVisible = true
                    }
                }
                else -> Unit
            }
        }
    }

    private fun checkForUpdatesManually() {
        if (updateDownloading) return
        val isZh = appLanguage == AppLanguage.ZH
        Toast.makeText(this, if (isZh) "正在检查更新…" else "Checking for updates…", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            when (val result = runCatching {
                UpdateChecker.check(applicationContext)
            }.getOrNull()) {
                is UpdateCheckResult.Available -> {
                    pendingUpdate = result.info
                    pendingApk = null
                    updateError = null
                    updateDialogVisible = true
                }
                UpdateCheckResult.UpToDate -> {
                    Toast.makeText(this@MainActivity, if (isZh) "已是最新版本" else "Already up to date", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this@MainActivity, if (isZh) "检查更新失败，请稍后重试" else "Update check failed, please try again later", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openDroidspacesDialog() {
        if (droidspacesDownloading) return
        droidspacesDialogVisible = true
        droidspacesRelease = null
        droidspacesSavedPath = null
        droidspacesError = null
        loadDroidspacesRelease()
    }

    private fun loadDroidspacesRelease() {
        droidspacesLoading = true
        droidspacesError = null
        lifecycleScope.launch {
            runCatching { DroidspacesModule.latest() }
                .onSuccess { droidspacesRelease = it }
                .onFailure {
                    droidspacesError = it.message ?: "Failed to fetch latest release"
                }
            droidspacesLoading = false
        }
    }

    private fun downloadDroidspacesModule() {
        val release = droidspacesRelease ?: return
        if (droidspacesDownloading) return
        droidspacesDownloading = true
        droidspacesProgress = 0f
        droidspacesError = null
        lifecycleScope.launch {
            runCatching {
                DroidspacesModule.download(applicationContext, release) { progress ->
                    droidspacesProgress = progress
                }
            }.onSuccess {
                droidspacesSavedPath = it
            }.onFailure {
                droidspacesError = it.message ?: "Download failed"
            }
            droidspacesDownloading = false
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes <= 0 -> "Unknown size"
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / 1024.0 / 1024.0)
        else -> "%.0f KB".format(bytes / 1024.0)
    }

    override fun onStop() {
        updateCheckedThisForeground = false
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderReceivedListenerSticky(binderListener)
        Stellar.addRequestPermissionResultListener(stellarPermissionListener)
        Stellar.addBinderReceivedListenerSticky(stellarBinderListener)
        binderReady = PrivService.pick() != null
        refreshAuth()
        antiRootGuard = getSharedPreferences("pyb110", MODE_PRIVATE).getBoolean("antiroot_guard", true)
        softRebootAfter = getSharedPreferences("pyb110", MODE_PRIVATE).getBoolean("soft_reboot_after", false)
        softRebootDelayMin = getSharedPreferences("pyb110", MODE_PRIVATE).getInt("soft_reboot_delay_min", 0)
        unloadGuard = getSharedPreferences("pyb110", MODE_PRIVATE).getBoolean("unload_guard", true)
        susfsSupport = getSharedPreferences("pyb110", MODE_PRIVATE).getBoolean("susfs_support", false)
        vfsSupport = getSharedPreferences("pyb110", MODE_PRIVATE).getBoolean("vfs_support", false)
        val prefs = getSharedPreferences("pyb110", MODE_PRIVATE)
        legalAccepted = prefs.getBoolean("legal_accepted", false)
        val savedLang = prefs.getString("app_language", null)
        val sysLang = java.util.Locale.getDefault().language
        appLanguage = when {
            savedLang != null -> AppLanguage.fromCode(savedLang)
            sysLang.startsWith("zh") -> AppLanguage.ZH
            sysLang.startsWith("ru") -> AppLanguage.RU
            sysLang.startsWith("pt") -> AppLanguage.PT
            else -> AppLanguage.EN
        }
        languageChosen = prefs.getBoolean("language_chosen", legalAccepted)
        amoledBlack = prefs.getBoolean("amoled_black", false)
        safeModeModules = prefs.getBoolean("safe_mode_modules", false)
        DynamicProfileManager.init(applicationContext)
        dynamicProfileCount = DynamicProfileManager.profileCount()
        bypassRebootGuard = prefs.getBoolean("bypass_reboot_guard", BuildConfig.VERSION_NAME.contains("Test", ignoreCase = true))
        if (bypassRebootGuard || BuildConfig.VERSION_NAME.contains("Test", ignoreCase = true)) {
            prefs.edit().remove("last_run_boot_id").apply()
            bootSessionRan = false
        } else {
            bootSessionRan = runDoneThisBoot()
        }
        betaChannel = prefs.getBoolean("beta_channel", UpdateChecker.isBetaChannel(this))
        val savedManager = prefs.getString("manager", null)
        manager = runCatching { ManagerKind.valueOf(savedManager ?: "") }.getOrDefault(ManagerKind.KERNELSU)
        kernelVersion = runCatching { android.system.Os.uname().release }.getOrDefault("Unknown")
        exploit = JailbreakCore.kindFor(kernelVersion)?.let {
            runCatching { ExploitKind.valueOf(it) }.getOrNull()
        } ?: ExploitKind.UNSUPPORTED
        chaseRetry = getSharedPreferences("pyb110", MODE_PRIVATE)
            .getBoolean("chase_retry", defaultChaseRetry())
        autoJailbreak = getSharedPreferences("pyb110", MODE_PRIVATE)
            .getBoolean("auto_jailbreak", false)
        if (manager == ManagerKind.DYNAMIC && !supportsUniversal()) {
            manager = ManagerKind.KERNELSU
        }
        dynamicSwitching = manager == ManagerKind.DYNAMIC
        hotManager = if (dynamicSwitching) ManagerKind.KERNELSU else manager
        customSpoofedPkg = prefs.getString("custom_spoofed_pkg", "") ?: ""
        blockOtaUpdates = prefs.getBoolean("block_ota_updates", false)
        deviceModel = DeviceNameResolver.getCachedOrLocalDeviceName(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val market = DeviceNameResolver.resolveDeviceNameOnline(applicationContext)
            withContext(Dispatchers.Main) {
                deviceModel = market
            }
        }
        refreshStaticStateAsync()
        refreshKsuAsync()
        setContent {
            CouiTheme(darkTheme = isSystemInDarkTheme()) {
                AppRoot()
            }
        }
    }

    /* ===== Root chrome ===== */

    @Composable
    private fun LegalSection(title: String, lines: List<String>) {
        Text(text = title, kind = CouiTextKind.SectionTitle, color = CouiTheme.colorScheme.accent)
        Spacer(Modifier.height(8.dp))
        for (line in lines) {
            Text(text = line, kind = CouiTextKind.Description, color = CouiTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
    }

    @Composable
    private fun LanguageGate(onAccept: (AppLanguage) -> Unit) {
        val scheme = CouiTheme.colorScheme
        var selected by remember { mutableStateOf(appLanguage) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(scheme.surface),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(Icons.Filled.Language, null, Modifier.size(38.dp), tint = scheme.accent)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = AppStrings.langGateTitle(selected),
                kind = CouiTextKind.PageTitle,
                color = scheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = AppStrings.langGateSubtitle(selected),
                kind = CouiTextKind.Description,
                color = scheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))

            val options = AppLanguage.entries
            for (opt in options) {
                val isSel = opt == selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSel) scheme.surface else scheme.card)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) scheme.accent else scheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selected = opt }
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = opt.nativeName,
                                kind = CouiTextKind.SectionTitle,
                                color = if (isSel) scheme.accent else scheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = opt.title,
                                kind = CouiTextKind.Description,
                                color = scheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) scheme.accent else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (isSel) scheme.accent else scheme.onSurface.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                AppIcon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = Color.White)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.accent)
                    .clickable { onAccept(selected) }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.langGateContinue(selected),
                    kind = CouiTextKind.SectionTitle,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    @Composable
    private fun LegalGate(onAccept: () -> Unit, onToggleLanguage: () -> Unit) {
        val scheme = CouiTheme.colorScheme
        val scroll = rememberScrollState()
        var input by remember { mutableStateOf("") }
        var laidOut by remember { mutableStateOf(false) }
        var readDone by remember { mutableStateOf(false) }
        val isZh = appLanguage == AppLanguage.ZH
        val legalPhrase = AppStrings.legalPhrase(appLanguage)

        LaunchedEffect(Unit) {
            withFrameNanos { }
            laidOut = true
        }
        val atBottom by remember {
            derivedStateOf { scroll.maxValue == 0 || scroll.value >= scroll.maxValue - 8 }
        }
        LaunchedEffect(laidOut, atBottom) {
            if (laidOut && atBottom) readDone = true
        }
        val matched = if (isZh) {
            input.trim() == legalPhrase
        } else {
            input.trim().equals(legalPhrase, ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.legalTitle(appLanguage),
                    kind = CouiTextKind.SectionTitle,
                    color = scheme.accent,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surface)
                        .border(1.dp, scheme.accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onToggleLanguage() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = appLanguage.nativeName,
                        kind = CouiTextKind.Description,
                        color = scheme.accent
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = AppStrings.legalIntro(appLanguage),
                kind = CouiTextKind.Description,
                color = scheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(scheme.surface, RoundedCornerShape(14.dp))
                    .verticalScroll(scroll)
                    .padding(16.dp)
            ) {
                LegalSection(
                    AppStrings.legalDisclaimerSectionTitle(appLanguage),
                    AppStrings.legalDisclaimerPoints(appLanguage)
                )
                LegalSection(
                    AppStrings.legalLicenseSectionTitle(appLanguage),
                    AppStrings.legalLicensePoints(appLanguage)
                )
                Text(
                    text = AppStrings.legalEnd(appLanguage),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    kind = CouiTextKind.Description,
                    color = scheme.onSurface.copy(alpha = 0.45f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = AppStrings.legalScrollPrompt(appLanguage),
                kind = CouiTextKind.Description,
                color = scheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.accent.copy(alpha = 0.08f))
                    .clickable { input = legalPhrase }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = legalPhrase, kind = CouiTextKind.Description, color = scheme.accent)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = AppStrings.legalTapToFill(appLanguage),
                        kind = CouiTextKind.Category,
                        color = scheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.card, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                decorationBox = { inner ->
                    if (input.isEmpty()) {
                        Text(
                            text = AppStrings.legalPlaceholder(appLanguage),
                            kind = CouiTextKind.Description,
                            color = scheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    inner()
                }
            )
            Spacer(Modifier.height(10.dp))
            val enabled = readDone && matched
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (enabled) scheme.accent else scheme.onSurface.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = enabled) { onAccept() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.legalConfirmButton(appLanguage),
                    kind = CouiTextKind.SectionTitle,
                    color = if (enabled) Color.White else scheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (!readDone) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = AppStrings.legalMustScroll(appLanguage),
                    kind = CouiTextKind.Description,
                    color = scheme.onSurface.copy(alpha = 0.6f)
                )
            } else if (input.isNotEmpty() && !matched) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = AppStrings.legalTextMismatch(appLanguage),
                    kind = CouiTextKind.Description,
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }

    @Composable
    private fun LanguageSelectDialog() {
        if (!showLanguageDialog) return
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent
        Dialog(
            onDismissRequest = { showLanguageDialog = false },
            title = AppStrings.chooseAppLanguage(appLanguage),
            confirmAction = CouiDialogAction(
                text = AppStrings.close(appLanguage),
                onClick = { showLanguageDialog = false }
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                for (opt in AppLanguage.entries) {
                    val isSel = opt == appLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) accent.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                appLanguage = opt
                                getSharedPreferences("pyb110", MODE_PRIVATE).edit()
                                    .putString("app_language", opt.code)
                                    .putBoolean("language_chosen", true)
                                    .apply()
                                showLanguageDialog = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = opt.nativeName,
                                kind = CouiTextKind.SectionTitle,
                                color = if (isSel) accent else scheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = opt.title,
                                kind = CouiTextKind.Description,
                                color = scheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (isSel) {
                            AppIcon(Icons.Filled.Check, null, Modifier.size(22.dp), tint = accent)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    @Composable
    private fun ParseResultDialog() {
        val result = parseResultDialog ?: return
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent
        val isSuccess = result is ParseResult.Success

        Dialog(
            onDismissRequest = { parseResultDialog = null },
            title = if (isSuccess) "Kernel Symbol Parser" else "Parser Error",
            confirmAction = CouiDialogAction(
                text = AppStrings.close(appLanguage),
                onClick = {
                    if (isSuccess) {
                        val p = (result as ParseResult.Success).profile
                        val finalModel = parsedDeviceModel.trim().ifEmpty { p.model }
                        if (finalModel != p.model) {
                            val finalProfile = p.copy(model = finalModel)
                            DynamicProfileManager.addProfile(applicationContext, finalProfile)
                        }
                    }
                    parseResultDialog = null
                },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            dismissAction = if (isSuccess) CouiDialogAction(
                text = AppStrings.submitProfileToGroup(appLanguage),
                onClick = {
                    val p = (result as ParseResult.Success).profile
                    val finalModel = parsedDeviceModel.trim().ifEmpty { p.model }
                    val finalProfile = p.copy(model = finalModel)
                    DynamicProfileManager.addProfile(applicationContext, finalProfile)
                    uploadDiagnosticReport(finalProfile.toJsonString())
                }
            ) else null,
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                when (result) {
                    is ParseResult.Success -> {
                        Text(
                            text = AppStrings.parseBootSuccess(result.banner, result.symbolsCount, appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = scheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (appLanguage == AppLanguage.ZH) "设备型号 / 名称：" else "Device Model / Name:",
                            kind = CouiTextKind.Category,
                            color = scheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        BasicTextField(
                            value = parsedDeviceModel,
                            onValueChange = { parsedDeviceModel = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(scheme.card, RoundedCornerShape(8.dp))
                                .border(1.dp, scheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (parsedDeviceModel.isEmpty()) {
                                    Text(
                                        text = "e.g. realme RMX3867 (RE5C86L1)",
                                        kind = CouiTextKind.Description,
                                        color = scheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                inner()
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = result.details,
                            kind = CouiTextKind.Description,
                            color = scheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    is ParseResult.Error -> {
                        Text(
                            text = AppStrings.parseBootFailed(result.message, appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SoftRebootConfirmDialog() {
        if (!softRebootDialogVisible) return
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent

        Dialog(
            onDismissRequest = { softRebootDialogVisible = false },
            title = AppStrings.softRebootNow(appLanguage),
            confirmAction = CouiDialogAction(
                text = AppStrings.confirmSoftReboot(appLanguage),
                onClick = {
                    softRebootDialogVisible = false
                    performSoftReboot()
                },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            dismissAction = CouiDialogAction(
                text = AppStrings.cancel(appLanguage),
                onClick = { softRebootDialogVisible = false }
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                Text(
                    text = AppStrings.softRebootExplanation(appLanguage),
                    kind = CouiTextKind.Description,
                    color = scheme.onSurface
                )
            }
        }
    }

    @Composable
    private fun RerunBlockedDialog() {
        if (!rerunBlockedDialogVisible) return
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent

        Dialog(
            onDismissRequest = { rerunBlockedDialogVisible = false },
            title = AppStrings.rerunBlockedTitle(appLanguage),
            confirmAction = CouiDialogAction(
                text = AppStrings.actionRunAnyway(appLanguage),
                onClick = {
                    rerunBlockedDialogVisible = false
                    clearBootSessionGuard()
                    runFlow()
                },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            dismissAction = CouiDialogAction(
                text = AppStrings.close(appLanguage),
                onClick = { rerunBlockedDialogVisible = false }
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                Text(
                    text = AppStrings.rerunBlockedMessage(appLanguage),
                    kind = CouiTextKind.Description,
                    color = scheme.onSurface
                )
            }
        }
    }

    @Composable
    private fun PatchLevelWarningDialog() {
        if (!patchLevelDialogVisible) return
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent

        Dialog(
            onDismissRequest = { patchLevelDialogVisible = false },
            title = AppStrings.patchLevelWarningTitle(appLanguage),
            confirmAction = CouiDialogAction(
                text = AppStrings.actionRunAnyway(appLanguage),
                onClick = {
                    patchLevelDialogVisible = false
                    patchWarningDismissed = true
                    runFlow()
                },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            dismissAction = CouiDialogAction(
                text = AppStrings.close(appLanguage),
                onClick = { patchLevelDialogVisible = false }
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                Text(
                    text = AppStrings.patchLevelWarningMessage(Build.VERSION.SECURITY_PATCH, appLanguage),
                    kind = CouiTextKind.Description,
                    color = scheme.onSurface
                )
            }
        }
    }

    @Composable
    private fun AppRoot() {
        if (!languageChosen) {
            LanguageGate(onAccept = { chosen ->
                appLanguage = chosen
                languageChosen = true
                getSharedPreferences("pyb110", MODE_PRIVATE).edit()
                    .putString("app_language", chosen.code)
                    .putBoolean("language_chosen", true)
                    .apply()
            })
            return
        }
        if (!legalAccepted) {
            LegalGate(
                onAccept = {
                    legalAccepted = true
                    getSharedPreferences("pyb110", MODE_PRIVATE).edit()
                        .putBoolean("legal_accepted", true).apply()
                },
                onToggleLanguage = {
                    val nextIdx = (appLanguage.ordinal + 1) % AppLanguage.entries.size
                    val next = AppLanguage.entries[nextIdx]
                    appLanguage = next
                    getSharedPreferences("pyb110", MODE_PRIVATE).edit()
                        .putString("app_language", next.code)
                        .apply()
                }
            )
            return
        }
        val dark = CouiTheme.isDark
        val scheme = CouiTheme.colorScheme
        val overlayVisible = showManual || showExtras || showDiagnostic
        BackHandler(
            enabled = aboutLicense != null || showManual || showExtras || showDiagnostic ||
                (tab == AppTab.ABOUT && aboutPage == AboutPage.LICENSES)
        ) {
            when {
                aboutLicense != null -> aboutLicense = null
                showManual -> showManual = false
                showExtras -> showExtras = false
                showDiagnostic -> showDiagnostic = false
                tab == AppTab.ABOUT && aboutPage == AboutPage.LICENSES -> aboutPage = AboutPage.HOME
            }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (amoledBlack && dark) Color.Black else scheme.background,
            topBar = {
                when {
                    showDiagnostic -> AppTopBar(AppStrings.kernelDiagnosticTitle(appLanguage), onBack = { showDiagnostic = false })
                    showExtras -> AppTopBar(AppStrings.titleExtras(appLanguage), onBack = { showExtras = false })
                    showManual -> AppTopBar(AppStrings.titleManual(appLanguage), onBack = { showManual = false })
                    tab == AppTab.ABOUT && aboutPage == AboutPage.LICENSES ->
                        AppTopBar(AppStrings.titleLicenses(appLanguage), onBack = { aboutPage = AboutPage.HOME })
                    else -> AppTopBar(
                        when (tab) {
                            AppTab.HOME -> "Shark Neo"
                            AppTab.DEVICES -> AppStrings.supportedDevicesTitle(appLanguage)
                            else -> tab.label(appLanguage)
                        }
                    )
                }
            },
            bottomBar = {
                if (!overlayVisible) {
                    AppBottomBar(selected = tab, onSelect = { tab = it })
                }
            }
        ) { inner ->
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        val dir = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(240)) { it / 4 * dir } + fadeIn(tween(240))) togetherWith
                            (slideOutHorizontally(tween(240)) { -it / 4 * dir } + fadeOut(tween(240)))
                    },
                    label = "tab",
                    modifier = Modifier.fillMaxSize()
                ) { current ->
                    when (current) {
                        AppTab.HOME -> HomeScreen(inner)
                        AppTab.DEVICES -> DevicesScreen(inner)
                        AppTab.LOGS -> LogScreen(inner)
                        AppTab.HISTORY -> HistoryScreen(inner)
                        AppTab.ABOUT -> AboutScreen(inner)
                    }
                }
                AnimatedVisibility(
                    visible = showManual,
                    enter = slideInHorizontally(tween(240)) { it } + fadeIn(tween(240)),
                    exit = slideOutHorizontally(tween(240)) { it } + fadeOut(tween(240))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CouiTheme.colorScheme.background)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        ManualScreen(inner)
                    }
                }
                AnimatedVisibility(
                    visible = showExtras,
                    enter = slideInHorizontally(tween(240)) { it } + fadeIn(tween(240)),
                    exit = slideOutHorizontally(tween(240)) { it } + fadeOut(tween(240))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CouiTheme.colorScheme.background)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        ExtrasScreen(inner)
                    }
                }
                AnimatedVisibility(
                    visible = showDiagnostic,
                    enter = slideInHorizontally(tween(240)) { it } + fadeIn(tween(240)),
                    exit = slideOutHorizontally(tween(240)) { it } + fadeOut(tween(240))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CouiTheme.colorScheme.background)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        DiagnosticScreen(inner)
                    }
                }
            }
        }
        UpdateDialog()
        DroidspacesDialog()
        LicenseDetailDialog()
        DonateDialog()
        LanguageSelectDialog()
        ParseResultDialog()
        SoftRebootConfirmDialog()
        RerunBlockedDialog()
        PatchLevelWarningDialog()
    }

    @Composable
    private fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    kind = CouiTextKind.Toolbar,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
            },
            navigationIcon = { if (onBack != null) CouiBackButton(onBack) }
        )
    }

    @Composable
    private fun CouiBackButton(onBack: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 34) {
            TopAppBarButton(onClick = onBack) {
                AppIcon(CouiIcons.Back, "Back", Modifier.size(24.dp))
            }
        } else {
            val dark = CouiTheme.isDark
            val source = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CouiShape(24.dp, 2f))
                    .couiButtonFeedback(dark, source, true)
                    .clickable(source, indication = null, role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Back" },
                contentAlignment = Alignment.Center
            ) {
                AppIcon(CouiIcons.Back, null, Modifier.size(24.dp))
            }
        }
    }

    @Composable
    private fun AppBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
        val renderer = remember {
            if (Build.VERSION.SDK_INT >= 34) {
                runCatching { CouiFloatingRenderer() }.getOrNull()
            } else {
                null
            }
        }
        if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                renderer = renderer,
                items = AppTab.entries.map { CouiNavigationItem(it.label(appLanguage), it.icon, it.icon) },
                selectedIndex = selected.ordinal,
                dark = CouiTheme.isDark,
                onSelection = { onSelect(AppTab.entries[it]) }
            )
        } else {
            PlainBottomBar(selected = selected, onSelect = onSelect)
        }
    }

    /* API 33 上底栏浮动材质不可用，使用同一套 COUI 配色的平铺栏位。 */
    @Composable
    private fun PlainBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
        val dark = CouiTheme.isDark
        val scheme = CouiTheme.colorScheme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surface)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .height(64.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { entry ->
                val source = remember { MutableInteractionSource() }
                val tint = if (entry == selected) scheme.accent else scheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 50))
                        .couiNavigationFeedback(dark, source)
                        .selectable(
                            selected = entry == selected,
                            interactionSource = source,
                            indication = null,
                            enabled = true,
                            role = Role.Tab
                        ) { onSelect(entry) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppIcon(entry.icon, null, Modifier.size(24.dp), tint = tint)
                    Text(text = entry.label(appLanguage), kind = CouiTextKind.Category, color = tint)
                }
            }
        }
    }

    /* ===== Shared pieces ===== */

    @Composable
    private fun AppIcon(
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        tint: Color = Color.Unspecified
    ) {
        val color = tint.takeOrElse {
            LocalCouiContentColor.current.takeOrElse { CouiTheme.colorScheme.onSurface }
        }
        Image(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = ColorFilter.tint(color)
        )
    }

    private fun itemDividerIndent(): Dp =
        CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing

    @Composable
    private fun Chevron() {
        val colors = CouiPageDefaults.themeColors()
        AppIcon(CouiIcons.Next, null, Modifier.padding(start = 4.dp).size(18.dp), tint = colors.summary)
    }

    /**
     * 统一设置行：图标 + 标题 + 可选摘要 + 尾部内容。
     * onClick 为空的行走只读装配，交互由尾部控件负责。
     */
    @Composable
    private fun SettingItem(
        icon: ImageVector,
        title: String,
        summary: String? = null,
        enabled: Boolean = true,
        dark: Boolean = CouiTheme.isDark,
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        trailing: @Composable () -> Unit = {}
    ) {
        val colors = CouiPageDefaults.themeColors()
        Item(
            modifier = modifier,
            icon = {
                AppIcon(
                    icon,
                    null,
                    Modifier.size(CouiPageDefaults.iconSize),
                    tint = if (enabled) colors.title else colors.disabled
                )
            },
            summary = if (summary == null) null else {
                {
                    Text(
                        text = summary,
                        kind = CouiTextKind.PreferenceSummary,
                        color = colors.summary,
                        textAlign = TextAlign.Start
                    )
                }
            },
            trailing = trailing,
            onClick = onClick,
            enabled = enabled,
            dark = dark
        ) {
            Text(
                text = title,
                kind = CouiTextKind.PreferenceTitle,
                color = if (enabled) colors.title else colors.disabled,
                textAlign = TextAlign.Start
            )
        }
    }

    @Composable
    private fun SettingSwitchItem(
        icon: ImageVector,
        title: String,
        checked: Boolean,
        summary: String? = null,
        onCheckedChange: (Boolean) -> Unit
    ) {
        SettingItem(
            icon = icon,
            title = title,
            summary = summary,
            onClick = { onCheckedChange(!checked) },
            trailing = {
                Switch(
                    checked = checked,
                    onCheckedChange = null,
                    onDragCheckedChange = null
                )
            }
        )
    }

    @Composable
    private fun CouiProgressBar(progress: Float) {
        val scheme = CouiTheme.colorScheme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(scheme.onSurface.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(scheme.accent)
            )
        }
    }

    @Composable
    private fun DialogBody(text: String, monospace: Boolean = false) {
        val scheme = CouiTheme.colorScheme
        val style = rememberCouiTextStyle(CouiTextKind.Body).let {
            if (monospace) it.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp) else it
        }
        SelectionContainer {
            Box(modifier = Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
                BasicText(
                    text = text,
                    style = style.copy(color = scheme.onSurface)
                )
            }
        }
    }

    @Composable
    private fun DialogLink(text: String, onClick: () -> Unit) {
        Text(
            text = text,
            kind = CouiTextKind.PreferenceTitle,
            color = CouiTheme.colorScheme.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp)
        )
    }

    /* ===== Dialogs ===== */

    @Composable
    private fun DroidspacesDialog() {
        val release = droidspacesRelease
        val savedPath = droidspacesSavedPath
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent
        val confirm = when {
            droidspacesDownloading -> CouiDialogAction(
                text = "Downloading ${(droidspacesProgress * 100).toInt()}%",
                onClick = {},
                enabled = false
            )
            droidspacesLoading -> CouiDialogAction(
                text = "Close",
                onClick = { droidspacesDialogVisible = false }
            )
            release == null -> CouiDialogAction(
                text = "Retry",
                onClick = { loadDroidspacesRelease() },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            )
            savedPath != null -> CouiDialogAction(
                text = "Open KernelSU",
                onClick = {
                    droidspacesDialogVisible = false
                    launchManager()
                },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            )
            else -> CouiDialogAction(
                text = "Download",
                onClick = { downloadDroidspacesModule() },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            )
        }
        val dismiss = if (droidspacesDownloading || droidspacesLoading) null else {
            CouiDialogAction(text = "Close", onClick = { droidspacesDialogVisible = false })
        }
        Dialog(
            onDismissRequest = { if (!droidspacesDownloading) droidspacesDialogVisible = false },
            title = "Droidspaces Module",
            confirmAction = confirm,
            dismissAction = dismiss,
            visible = droidspacesDialogVisible
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                when {
                    droidspacesLoading -> Text(
                        text = "Fetching latest release…",
                        kind = CouiTextKind.Body,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                    release == null -> Text(
                        text = droidspacesError ?: "Failed to fetch latest release",
                        kind = CouiTextKind.Body,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                    else -> {
                        Text(
                            text = "Latest version ${release.version}",
                            kind = CouiTextKind.PreferenceTitle,
                            color = scheme.onSurface,
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${release.assetName} · ${formatSize(release.assetSize)}",
                            kind = CouiTextKind.PreferenceSummary,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(12.dp))
                        if (savedPath == null) {
                            DialogBody(release.changelog.ifBlank { "No release notes" })
                        } else {
                            Text(
                                text = "Saved to:",
                                kind = CouiTextKind.PreferenceSummary,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(4.dp))
                            SelectionContainer {
                                BasicText(
                                    text = savedPath,
                                    style = rememberCouiTextStyle(CouiTextKind.Body).copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = scheme.onSurface
                                    )
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Open KernelSU Manager → Modules → Install, then select this file to flash.",
                                kind = CouiTextKind.Body,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                        }
                        droidspacesError?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it,
                                kind = CouiTextKind.Description,
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Start
                            )
                        }
                        if (droidspacesDownloading) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Downloading ${(droidspacesProgress * 100).toInt()}%",
                                kind = CouiTextKind.Body,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(6.dp))
                            CouiProgressBar(droidspacesProgress)
                        }
                    }
                }
            }
        }
    }

    private fun installApk(file: File): Boolean {
        if (!packageManager.canRequestPackageInstalls()) {
            Toast.makeText(this, "Allow installing unknown apps, then tap Install again", Toast.LENGTH_LONG).show()
            runCatching {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")
                    )
                )
            }
            return false
        }
        return runCatching {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true
        }.getOrElse {
            updateError = "Unable to launch installer: ${it.message}"
            false
        }
    }

    @Composable
    private fun UpdateDialog() {
        val info = pendingUpdate ?: return
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent
        val isZh = appLanguage == AppLanguage.ZH
        Dialog(
            onDismissRequest = { if (!updateDownloading) updateDialogVisible = false },
            title = "${AppStrings.updateAvailable(appLanguage)} ${info.version}",
            confirmAction = CouiDialogAction(
                text = when {
                    updateDownloading -> if (isZh) "下载中 ${(updateProgress * 100).toInt()}%" else "Downloading ${(updateProgress * 100).toInt()}%"
                    pendingApk != null -> if (isZh) "安装" else "Install"
                    else -> AppStrings.updateNow(appLanguage)
                },
                onClick = {
                    val ready = pendingApk
                    if (ready != null) {
                        if (installApk(ready)) updateDialogVisible = false
                    } else {
                        updateDownloading = true
                        updateError = null
                        updateProgress = 0f
                        scope.launch {
                            val result = runCatching {
                                UpdateChecker.download(context, info) { p -> updateProgress = p }
                            }
                            updateDownloading = false
                            result.onSuccess { file ->
                                if (!UpdateChecker.verifySignature(context, file)) {
                                    file.delete()
                                    updateError = if (isZh) "APK 签名与当前应用不匹配" else "APK signature does not match the current app"
                                } else {
                                    pendingApk = file
                                    if (installApk(file)) updateDialogVisible = false
                                }
                            }.onFailure {
                                updateError = it.message ?: (if (isZh) "下载失败" else "Download failed")
                            }
                        }
                    }
                },
                enabled = !updateDownloading,
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            dismissAction = CouiDialogAction(
                text = if (isZh) "稍后" else "Later",
                onClick = { updateDialogVisible = false },
                enabled = !updateDownloading
            ),
            visible = updateDialogVisible
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                Text(
                    text = "${AppStrings.currentVersionLabel(appLanguage)} ${BuildConfig.VERSION_NAME}  ·  Source ${info.source}",
                    kind = CouiTextKind.PreferenceSummary,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
                if (updateDownloading) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (isZh) "下载中 ${(updateProgress * 100).toInt()}%" else "Downloading ${(updateProgress * 100).toInt()}%",
                        kind = CouiTextKind.Body,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(6.dp))
                    CouiProgressBar(updateProgress)
                } else {
                    Spacer(Modifier.height(12.dp))
                    DialogBody(info.changelog.ifBlank { if (isZh) "暂无更新日志" else "No release notes" })
                    updateError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            kind = CouiTextKind.Description,
                            color = Color(0xFFD32F2F),
                            textAlign = TextAlign.Start
                        )
                        DialogLink(AppStrings.downloadBrowser(appLanguage)) { uriHandler.openUri(info.pageUrl) }
                    }
                }
            }
        }
    }

    @Composable
    private fun DonateDialog() {
        if (!showDonate) return
        val accent = CouiTheme.colorScheme.accent
        Dialog(
            onDismissRequest = { showDonate = false },
            title = AppStrings.supportDeveloper(appLanguage),
            confirmAction = CouiDialogAction(
                text = AppStrings.close(appLanguage),
                onClick = { showDonate = false },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DonateQr(
                        label = "WeChat Pay",
                        assetPath = "donate/wechat.png",
                        modifier = Modifier.weight(1f)
                    )
                    DonateQr(
                        label = "Alipay",
                        assetPath = "donate/alipay.png",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    private fun DonateQr(label: String, assetPath: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val bitmap = remember(assetPath) {
            runCatching {
                context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            }.getOrNull()
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Text(
                text = label,
                kind = CouiTextKind.PreferenceSummary,
                color = CouiTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    private fun LicenseDetailDialog() {
        val item = aboutLicense ?: return
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val scheme = CouiTheme.colorScheme
        val accent = scheme.accent
        val contents = remember(item) {
            item.licenses.map { license ->
                val body = license.content?.takeIf { it.isNotBlank() }
                    ?: license.asset?.let { asset ->
                        runCatching {
                            context.assets.open(asset).bufferedReader().use { it.readText() }
                        }.getOrNull()
                    }
                license to body
            }
        }
        Dialog(
            onDismissRequest = { aboutLicense = null },
            title = item.name,
            confirmAction = CouiDialogAction(
                text = "Close",
                onClick = { aboutLicense = null },
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(accent)
            ),
            visible = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                if (item.licenses.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.licenses.forEach { LicenseChip(it.name) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                SelectionContainer {
                    Box(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (contents.isEmpty()) {
                                Text(
                                    text = "No license text declared",
                                    kind = CouiTextKind.Body,
                                    color = scheme.onSurface,
                                    textAlign = TextAlign.Start
                                )
                            }
                            contents.forEach { (license, body) ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = license.name,
                                        kind = CouiTextKind.PreferenceTitle,
                                        color = accent,
                                        textAlign = TextAlign.Start
                                    )
                                    BasicText(
                                        text = body ?: "No bundled license text",
                                        style = rememberCouiTextStyle(CouiTextKind.Body).copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = scheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                item.website?.let { url ->
                    DialogLink("Visit Homepage") { uriHandler.openUri(url) }
                }
            }
        }
    }

    /* ===== Home ===== */

    @Composable
    private fun HomeScreen(inner: PaddingValues) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 4.dp,
                bottom = inner.calculateBottomPadding() + 8.dp
            )
        ) {
            item {
                StatusCard(granted = binderReady && authorized)
            }
            if (exploit == ExploitKind.UNSUPPORTED) {
                item {
                    UnsupportedKernelBanner(onClick = { showDiagnostic = true })
                }
            } else if (JailbreakCore.isSecurityPatchPatched(engineKind = exploit.name)) {
                item {
                    PatchedFirmwareBanner(onClick = { patchLevelDialogVisible = true })
                }
            }
            item {
                PrimaryActionCard()
            }
            if (ksuActive) {
                item {
                    SoftRebootCard()
                }
            }
            item {
                HomeEntriesCard()
            }
            item {
                InfoCard(
                    kernelVersion = kernelVersion,
                    exploitLabel = if (exploit == ExploitKind.UNSUPPORTED) AppStrings.unsupportedDevice(appLanguage) else exploit.fullLabel,
                    deviceModel = deviceModel,
                    fingerprint = Build.FINGERPRINT,
                    selinux = selinuxText
                )
            }
        }
    }

    @Composable
    private fun PatchedFirmwareBanner(onClick: () -> Unit) {
        val dark = CouiTheme.isDark
        val colors = CouiPageDefaults.themeColors(dark)
        val amberBg = if (dark) Color(0xFF332500) else Color(0xFFFFF8E1)
        val amberTint = Color(0xFFFFB300)

        Group(colors = colors.copy(card = amberBg)) {
            Item(
                onClick = onClick,
                trailing = { Chevron() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        icon = Icons.Filled.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = amberTint
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.patchLevelBannerTitle(appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = AppStrings.patchLevelBannerSummary(Build.VERSION.SECURITY_PATCH, appLanguage),
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UnsupportedKernelBanner(onClick: () -> Unit) {
        val dark = CouiTheme.isDark
        val colors = CouiPageDefaults.themeColors(dark)
        val amberBg = if (dark) Color(0xFF332500) else Color(0xFFFFF8E1)
        val amberTint = Color(0xFFFFB300)

        Group(colors = colors.copy(card = amberBg)) {
            Item(
                onClick = onClick,
                trailing = { Chevron() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        icon = Icons.Filled.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = amberTint
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.unsupportedKernelBannerTitle(appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = AppStrings.unsupportedKernelBannerDesc(appLanguage),
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            Item(
                onClick = { launchBootPicker() },
                enabled = !parsingBoot,
                trailing = { Chevron() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        icon = Icons.Filled.Memory,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = amberTint
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (parsingBoot) AppStrings.parsingBootTitle(appLanguage) else AppStrings.parseBootImage(appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = AppStrings.parseBootImageSummary(appLanguage),
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            Item(
                onClick = { uploadDiagnosticReport() },
                enabled = !uploadingReport,
                trailing = { Chevron() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        icon = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = amberTint
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uploadingReport) AppStrings.submittingReport(appLanguage) else AppStrings.submitDeviceForSupport(appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "https://t.me/sharkmotosupport (Direct Telegram support)",
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun StatusCard(granted: Boolean) {
        val dark = CouiTheme.isDark
        val colors = CouiPageDefaults.themeColors(dark)
        val backendName = PrivService.pick()?.label ?: "Stellar"
        val green = if (dark) Color(0xFF1A3825) else Color(0xFFDFFAE4)
        val greenTint = Color(0xFF36D167)
        Group(colors = colors.copy(card = if (granted) green else colors.card)) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column {
                        Text(
                            text = if (granted) AppStrings.statusAuthorized(backendName, appLanguage)
                            else AppStrings.statusNotAuthorized(backendName, appLanguage),
                            kind = CouiTextKind.PageTitle,
                            color = colors.title,
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (granted) AppStrings.statusReadyDesc(backendName, appLanguage)
                            else AppStrings.statusNotReadyDesc(backendName, appLanguage),
                            kind = CouiTextKind.Body,
                            color = colors.title,
                            textAlign = TextAlign.Start
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(end = 20.dp, bottom = 12.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AppIcon(
                        Icons.Rounded.CheckCircleOutline,
                        null,
                        Modifier.size(100.dp),
                        tint = if (granted) greenTint else Color(0xFF9AA0A6)
                    )
                }
            }
        }
    }

    @Composable
    private fun PrimaryActionCard() {
        val colors = CouiPageDefaults.themeColors()
        val alreadyRan = if (bypassRebootGuard || BuildConfig.VERSION_NAME.contains("Test", ignoreCase = true)) false else (bootSessionRan || runDoneThisBoot())
        val label = when {
            busy -> AppStrings.actionRunning(appLanguage)
            !binderReady -> AppStrings.actionStellarNotRunning(appLanguage)
            !authorized -> AppStrings.actionAuthorizeShizuku(appLanguage)
            alreadyRan -> AppStrings.rebootRequiredAlreadyRan(appLanguage)
            exploit == ExploitKind.UNSUPPORTED -> AppStrings.actionDiagnoseAndReport(appLanguage)
            ksuActive -> AppStrings.actionReRoot(appLanguage)
            else -> AppStrings.actionOneClickRoot(appLanguage)
        }
        val isWarning = alreadyRan && !busy && binderReady && authorized
        Group {
            Item(
                onClick = {
                    if (!busy) {
                        when {
                            !binderReady -> {
                                addLog("Start Stellar or Shizuku first")
                                PrivService.openManager(applicationContext)
                            }
                            !authorized -> PrivService.request(REQ_SHIZUKU)
                            alreadyRan -> {
                                addLog("[boot-guard] Refusing rerun in the same boot session - device reboot required")
                                rerunBlockedDialogVisible = true
                            }
                            exploit == ExploitKind.UNSUPPORTED -> {
                                showDiagnostic = true
                            }
                            JailbreakCore.isSecurityPatchPatched(engineKind = exploit.name) && !patchWarningDismissed -> {
                                addLog("[preflight] Device security patch is ${Build.VERSION.SECURITY_PATCH} (>= Nov 2026 threshold)")
                                patchLevelDialogVisible = true
                            }
                            else -> runFlow()
                        }
                    }
                },
                enabled = !busy,
                trailing = { Chevron() }
            ) {
                Text(
                    text = label,
                    kind = CouiTextKind.SectionTitle,
                    color = when {
                        busy -> colors.disabled
                        isWarning -> Color(0xFFD32F2F)
                        else -> colors.accent
                    },
                    textAlign = TextAlign.Start
                )
            }
        }
    }

    @Composable
    private fun SoftRebootCard() {
        val colors = CouiPageDefaults.themeColors()
        Group {
            Item(
                onClick = { softRebootDialogVisible = true },
                enabled = !busy && !softRebooting,
                trailing = { Chevron() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        icon = Icons.Filled.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = colors.accent
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (softRebooting) AppStrings.actionRunning(appLanguage) else AppStrings.softRebootNow(appLanguage),
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = AppStrings.softRebootNowSummary(appLanguage),
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HomeEntriesCard() {
        Group {
            DropdownItem(
                icon = Icons.Filled.Tag,
                title = AppStrings.rootManager(appLanguage),
                values = managerChoices(),
                selected = manager,
                label = { it.displayLabel(appLanguage) }
            ) { selectManager(it) }
            Divider(startIndent = itemDividerIndent())
            SettingItem(
                icon = Icons.Filled.Tune,
                title = AppStrings.extras(appLanguage),
                onClick = { showExtras = true },
                trailing = { Chevron() }
            )
            Divider(startIndent = itemDividerIndent())
            SettingItem(
                icon = Icons.Filled.MenuBook,
                title = AppStrings.manual(appLanguage),
                summary = AppStrings.manualSummary(appLanguage),
                onClick = { showManual = true },
                trailing = { Chevron() }
            )
            Divider(startIndent = itemDividerIndent())
            SettingItem(
                icon = Icons.Filled.BugReport,
                title = AppStrings.kernelDiagnosticTitle(appLanguage),
                summary = AppStrings.kernelDiagnosticSummary(appLanguage),
                onClick = { showDiagnostic = true },
                trailing = { Chevron() }
            )
        }
    }

    @Composable
    private fun <T> DropdownItem(
        icon: ImageVector,
        title: String,
        values: List<T>,
        selected: T,
        label: (T) -> String,
        onSelect: (T) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        val anchor = rememberCouiMenuAnchor()
        val colors = CouiPageDefaults.themeColors()
        Box {
            SettingItem(
                icon = icon,
                title = title,
                modifier = Modifier.couiMenuAnchor(anchor),
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label(selected),
                            kind = CouiTextKind.PreferenceValue,
                            color = colors.summary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Chevron()
                    }
                },
                onClick = { anchor.open { expanded = true } }
            )
            Menu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                items = values.map { CouiMenuItem(value = it, text = label(it), selected = it == selected) },
                onItemClick = { item ->
                    onSelect(item.value)
                    expanded = false
                },
                anchor = anchor
            )
        }
    }

    @Composable
    private fun InfoCard(
        kernelVersion: String,
        exploitLabel: String,
        deviceModel: String,
        fingerprint: String,
        selinux: String
    ) {
        val colors = CouiPageDefaults.themeColors()
        val activeProfile = DynamicProfileManager.lookup(kernelVersion)
        Group {
            SettingItem(Icons.Filled.DeveloperBoard, AppStrings.kernelVersion(appLanguage), summary = kernelVersion)
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            SettingItem(
                Icons.Filled.BugReport,
                AppStrings.exploitMethod(appLanguage),
                summary = if (activeProfile != null) "$exploitLabel • ${activeProfile.model}" else exploitLabel
            )
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            SettingItem(
                Icons.Filled.Smartphone,
                AppStrings.deviceModel(appLanguage),
                summary = if (activeProfile != null && !deviceModel.contains(activeProfile.model.substringBefore(" ("))) {
                    "$deviceModel [${activeProfile.model}]"
                } else deviceModel
            )
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            SettingItem(Icons.Filled.Fingerprint, AppStrings.buildFingerprint(appLanguage), summary = fingerprint)
            Divider(startIndent = itemDividerIndent(), color = colors.divider)
            SettingItem(Icons.Filled.Security, AppStrings.selinuxStatus(appLanguage), summary = selinux)
        }
    }

    /* ===== Extras ===== */

    @Composable
    private fun ExtrasScreen(inner: PaddingValues) {
        val colors = CouiPageDefaults.themeColors()
        LaunchedEffect(exploit) {
            if (!supportsUniversal()) {
                universalLoaded = false
                return@LaunchedEffect
            }
            val (rc, text) = withContext(Dispatchers.IO) {
                runRootCommand("cat /proc/kernelsu_manager", 20)
            }
            universalLoaded = rc == 0 && text.isNotEmpty()
            if (!universalLoaded) {
                addLog("Universal KO check failed: rc=$rc ${text.take(80)}")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 4.dp,
                bottom = inner.calculateBottomPadding() + 16.dp
            )
        ) {
            // Category 1: Jailbreak & System Guard
            if (exploit != ExploitKind.UNSUPPORTED) {
                item {
                    SectionLabel(AppStrings.extrasSectionJailbreak(appLanguage))
                    Group {
                        SettingSwitchItem(
                            icon = Icons.Filled.PowerSettingsNew,
                            title = AppStrings.autoJailbreak(appLanguage),
                            checked = autoJailbreak,
                            onCheckedChange = { updateAutoJailbreak(it) }
                        )
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingSwitchItem(
                            icon = Icons.Filled.Block,
                            title = AppStrings.blockOtaUpdatesTitle(appLanguage),
                            summary = AppStrings.blockOtaUpdatesSummary(appLanguage),
                            checked = blockOtaUpdates,
                            onCheckedChange = { checked ->
                                blockOtaUpdates = checked
                                getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("block_ota_updates", checked).apply()
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val (success, total) = JailbreakCore.setBlockOtaUpdates(checked) { cmd -> runRootCommand(cmd, 5) }
                                    withContext(Dispatchers.Main) {
                                        if (checked) {
                                            val msg = AppStrings.blockOtaSuccess(success, appLanguage)
                                            addLog("[ota-blocker] $msg ($success/$total components disabled)")
                                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            val msg = AppStrings.unblockOtaSuccess(appLanguage)
                                            addLog("[ota-blocker] $msg")
                                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        )
                        if (!hidesOplusOnlyOptions()) {
                            Divider(startIndent = itemDividerIndent(), color = colors.divider)
                            SettingSwitchItem(
                                icon = Icons.Filled.NotificationsOff,
                                title = AppStrings.disableSecurityAlerts(appLanguage),
                                checked = antiRootGuard,
                                onCheckedChange = { updateAntiRootGuard(it) }
                            )
                            Divider(startIndent = itemDividerIndent(), color = colors.divider)
                            SettingSwitchItem(
                                icon = Icons.Filled.RemoveModerator,
                                title = AppStrings.unloadKernelGuard(appLanguage),
                                checked = unloadGuard,
                                onCheckedChange = { updateUnloadGuard(it) }
                            )
                        }
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingSwitchItem(
                            icon = Icons.Filled.Refresh,
                            title = AppStrings.bypassRebootGuardTitle(appLanguage),
                            summary = AppStrings.bypassRebootGuardSummary(appLanguage),
                            checked = bypassRebootGuard,
                            onCheckedChange = { updateBypassRebootGuard(it) }
                        )
                        if (supportsChaseRetry()) {
                            Divider(startIndent = itemDividerIndent(), color = colors.divider)
                            SettingSwitchItem(
                                icon = Icons.Filled.Refresh,
                                title = AppStrings.chaseRetry(appLanguage),
                                checked = chaseRetry,
                                onCheckedChange = { updateChaseRetry(it) }
                            )
                        }
                    }
                }
            }

            // Category 2: Root Managers & Modules
            item {
                SectionLabel(AppStrings.extrasSectionManagers(appLanguage))
                Group {
                    if (supportsUniversal() && universalLoaded) {
                        DropdownItem(
                            icon = Icons.Filled.SwapHoriz,
                            title = AppStrings.hotSwitchManager(appLanguage),
                            values = hotManagerChoices(),
                            selected = hotManager,
                            label = { it.displayLabel(appLanguage) }
                        ) { target ->
                            hotManager = target
                            switchHotManager(target)
                        }
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    }
                    SettingItem(
                        icon = Icons.Filled.Security,
                        title = AppStrings.spoofedManager(appLanguage),
                        summary = if (customSpoofedPkg.isNotBlank()) customSpoofedPkg else AppStrings.spoofedManagerScan(appLanguage),
                        onClick = {
                            val found = JailbreakCore.findSpoofedNextPackage(this@MainActivity) { cmd -> runRootCommand(cmd, 5) }
                            if (!found.isNullOrEmpty()) {
                                customSpoofedPkg = found
                                getSharedPreferences("pyb110", MODE_PRIVATE).edit().putString("custom_spoofed_pkg", found).apply()
                                addLog(AppStrings.spoofedManagerDetected(found, appLanguage))
                                Toast.makeText(this@MainActivity, AppStrings.spoofedManagerDetected(found, appLanguage), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, AppStrings.spoofedManagerNotFound(appLanguage), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingSwitchItem(
                        icon = Icons.Filled.HealthAndSafety,
                        title = AppStrings.safeModeModules(appLanguage),
                        summary = AppStrings.safeModeSummary(appLanguage),
                        checked = safeModeModules,
                        onCheckedChange = { checked ->
                            safeModeModules = checked
                            getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("safe_mode_modules", checked).apply()
                            lifecycleScope.launch(Dispatchers.IO) {
                                JailbreakCore.setKernelSuSafeMode(checked) { cmd -> runRootCommand(cmd, 5) }
                            }
                        }
                    )
                    if (supportsSusfs()) {
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingSwitchItem(
                            icon = Icons.Filled.Extension,
                            title = AppStrings.susfsSupport(appLanguage),
                            checked = susfsSupport,
                            onCheckedChange = { updateSusfsSupport(it) }
                        )
                    }
                    if (exploit == ExploitKind.CVE61821) {
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingSwitchItem(
                            icon = Icons.Filled.AccountTree,
                            title = AppStrings.vfsSupport(appLanguage),
                            checked = vfsSupport,
                            onCheckedChange = { updateVfsSupport(it) }
                        )
                    }
                }
            }

            // Category 3: Diagnostics & System Tools
            item {
                SectionLabel(AppStrings.extrasSectionDiagnostics(appLanguage))
                Group {
                    SettingItem(
                        icon = Icons.Filled.BugReport,
                        title = AppStrings.kernelDiagnosticTitle(appLanguage),
                        summary = AppStrings.kernelDiagnosticSummary(appLanguage),
                        onClick = { showDiagnostic = true },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Share,
                        title = AppStrings.exportKernelInfo(appLanguage),
                        onClick = {
                            val info = """
                                Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})
                                Fingerprint: ${Build.FINGERPRINT}
                                Kernel: $kernelVersion
                                SELinux: $selinuxText
                                Manager: ${manager.displayLabel(appLanguage)}
                                Shark: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                            """.trimIndent()
                            val cm = getSystemService(ClipboardManager::class.java)
                            cm?.setPrimaryClip(ClipData.newPlainText("Shark Kernel Info", info))
                            Toast.makeText(this@MainActivity, AppStrings.kernelInfoCopied(appLanguage), Toast.LENGTH_SHORT).show()
                        }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingSwitchItem(
                        icon = Icons.Filled.Security,
                        title = AppStrings.selinuxMode(appLanguage),
                        summary = if (selinuxPermissive) "Permissive" else "Enforcing",
                        checked = selinuxPermissive,
                        onCheckedChange = { checked ->
                            lifecycleScope.launch {
                                val (rc, _) = withContext(Dispatchers.IO) {
                                    JailbreakCore.setSELinuxMode(!checked) { cmd -> runRootCommand(cmd, 5) }
                                }
                                if (rc == 0) {
                                    selinuxPermissive = checked
                                    selinuxText = if (checked) "Permissive" else "Enforcing"
                                    val modeName = if (checked) "Permissive" else "Enforcing"
                                    Toast.makeText(this@MainActivity, AppStrings.selinuxSwitched(modeName, appLanguage), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingSwitchItem(
                        icon = Icons.Filled.RestartAlt,
                        title = AppStrings.softRebootAfterRoot(appLanguage),
                        checked = softRebootAfter,
                        onCheckedChange = { updateSoftRebootAfter(it) }
                    )
                    if (softRebootAfter) {
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        DropdownItem(
                            icon = Icons.Filled.Schedule,
                            title = AppStrings.softRebootDelay(appLanguage),
                            values = softRebootDelays,
                            selected = softRebootDelayMin,
                            label = { softRebootDelayLabel(it) },
                            onSelect = { updateSoftRebootDelay(it) }
                        )
                    }
                }
            }

            // Category 4: Dynamic Profiles & Updates
            item {
                SectionLabel(AppStrings.extrasSectionProfiles(appLanguage))
                Group {
                    SettingItem(
                        icon = Icons.Filled.Download,
                        title = if (syncingProfiles) AppStrings.actionRunningDiagnostic(appLanguage) else AppStrings.syncProfiles(appLanguage),
                        summary = "${AppStrings.syncProfilesSummary(appLanguage)} (${dynamicProfileCount} active)",
                        onClick = { syncProfilesOnline() },
                        enabled = !syncingProfiles && !syncingGhostLock,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Refresh,
                        title = if (syncingGhostLock) AppStrings.actionRunningDiagnostic(appLanguage) else AppStrings.syncGhostLockTitle(appLanguage),
                        summary = AppStrings.syncGhostLockSummary(appLanguage),
                        onClick = { syncGhostLockProfilesOnline() },
                        enabled = !syncingProfiles && !syncingGhostLock,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Memory,
                        title = if (parsingBoot) AppStrings.parsingBootTitle(appLanguage) else AppStrings.parseBootImage(appLanguage),
                        summary = AppStrings.parseBootImageSummary(appLanguage),
                        onClick = { launchBootPicker() },
                        enabled = !parsingBoot,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingSwitchItem(
                        icon = Icons.Filled.Extension,
                        title = AppStrings.betaChannel(appLanguage),
                        summary = AppStrings.betaChannelSummary(appLanguage),
                        checked = betaChannel,
                        onCheckedChange = { toggleBetaChannel(it) }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingSwitchItem(
                        icon = Icons.Filled.DarkMode,
                        title = AppStrings.amoledMode(appLanguage),
                        summary = AppStrings.amoledSummary(appLanguage),
                        checked = amoledBlack,
                        onCheckedChange = { checked ->
                            amoledBlack = checked
                            getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("amoled_black", checked).apply()
                        }
                    )
                }
            }

            item {
                hotSwitchError?.let {
                    Text(
                        text = it,
                        kind = CouiTextKind.Description,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 6.dp)
                    )
                }
            }
            if (exploit == ExploitKind.CVE61821) {
                item {
                    Group {
                        SettingItem(
                            icon = Icons.Filled.Extension,
                            title = "Droidspaces Support",
                            summary = "Download the latest droidspaces_lkm module to Download/Shark, then flash it in KernelSU Manager."
                        )
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingItem(
                            icon = Icons.Filled.Download,
                            title = "Download Module",
                            onClick = { openDroidspacesDialog() },
                            trailing = { Chevron() }
                        )
                    }
                }
            }
        }
    }

    /* ===== Diagnostic & Device Support ===== */

    private fun toggleBetaChannel(enabled: Boolean) {
        betaChannel = enabled
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("beta_channel", enabled).apply()
        Toast.makeText(
            this,
            if (enabled) "Switched to Beta update channel" else "Switched to Stable update channel",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun syncProfilesOnline() {
        if (syncingProfiles) return
        syncingProfiles = true
        addLog("[sync] Fetching latest dynamic profiles from Shark repository...")
        lifecycleScope.launch {
            val result = DynamicProfileManager.syncRemote(applicationContext)
            // Also attempt to check GhostLock repo for matching kernel profile
            val glMatch = runCatching {
                GhostLockRepoSync.syncCurrentKernel(applicationContext, kernelVersion).getOrNull()
            }.getOrNull()

            syncingProfiles = false
            result.onSuccess { count ->
                dynamicProfileCount = DynamicProfileManager.profileCount()
                val newKind = JailbreakCore.kindFor(kernelVersion)?.let {
                    runCatching { ExploitKind.valueOf(it) }.getOrNull()
                } ?: ExploitKind.UNSUPPORTED
                exploit = newKind
                val glNotice = if (glMatch != null) " (+GhostLock matched for active kernel)" else ""
                Toast.makeText(this@MainActivity, AppStrings.syncProfilesSuccess(count, appLanguage) + glNotice, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@MainActivity, AppStrings.syncProfilesFailed(appLanguage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun syncGhostLockProfilesOnline() {
        if (syncingGhostLock) return
        syncingGhostLock = true
        addLog("[ghostlock-sync] Querying YuKongA/ghostlock-app repository...")
        lifecycleScope.launch {
            val result = GhostLockRepoSync.syncAllFromRepo(applicationContext, kernelVersion)
            syncingGhostLock = false
            result.onSuccess { summary ->
                dynamicProfileCount = DynamicProfileManager.profileCount()
                val newKind = JailbreakCore.kindFor(kernelVersion)?.let {
                    runCatching { ExploitKind.valueOf(it) }.getOrNull()
                } ?: exploit
                exploit = newKind
                val msg = if (summary.currentDeviceMatched) {
                    AppStrings.syncGhostLockMatched(summary.totalImported, summary.currentDeviceModel ?: "Device", appLanguage)
                } else {
                    AppStrings.syncGhostLockSuccess(summary.totalImported, appLanguage)
                }
                addLog("[ghostlock-sync] $msg")
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
            }.onFailure {
                addLog("[ghostlock-sync] Error: ${it.message}")
                Toast.makeText(this@MainActivity, AppStrings.syncProfilesFailed(appLanguage) + ": " + it.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchBootPicker() {
        runCatching {
            bootPickerLauncher.launch(arrayOf("*/*"))
        }.onFailure {
            Toast.makeText(this, "Cannot open file picker: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseBootUri(uri: Uri) {
        if (parsingBoot) return
        parsingBoot = true
        addLog("[boot-parser] Analyzing file from $uri...")
        lifecycleScope.launch {
            val res = BootSymbolParser.parseFromUri(applicationContext, uri)
            parsingBoot = false
            when (res) {
                is ParseResult.Success -> {
                    parsedDeviceModel = res.profile.model.removeSuffix(" (Imported)").trim()
                    DynamicProfileManager.addProfile(applicationContext, res.profile)
                    dynamicProfileCount = DynamicProfileManager.profileCount()
                    val newKind = JailbreakCore.kindFor(res.profile.release)?.let {
                        runCatching { ExploitKind.valueOf(it) }.getOrNull()
                    } ?: JailbreakCore.kindFor(kernelVersion)?.let {
                        runCatching { ExploitKind.valueOf(it) }.getOrNull()
                    } ?: ExploitKind.UNSUPPORTED
                    exploit = newKind
                    parseResultDialog = res
                    addLog("[boot-parser] Success: ${res.banner} (${res.symbolsCount} symbols recovered)")
                    Toast.makeText(this@MainActivity, AppStrings.parseBootSuccess(res.banner, res.symbolsCount, appLanguage), Toast.LENGTH_LONG).show()
                }
                is ParseResult.Error -> {
                    parseResultDialog = res
                    addLog("[boot-parser] Error: ${res.message}")
                    Toast.makeText(this@MainActivity, AppStrings.parseBootFailed(res.message, appLanguage), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performSoftReboot() {
        if (softRebooting) return
        softRebooting = true
        addLog("[soft-reboot] Initiating framework restart (Zygote)...")
        lifecycleScope.launch(Dispatchers.IO) {
            val cmd = "setprop ctl.restart zygote || setprop ctl.restart zygote_secondary || killall -9 zygote zygote64"
            val res = runRootCommand(cmd, 5)
            if (res.first != 0) {
                val fallback = runCatching {
                    val p = PrivService.newProcess(
                        arrayOf("su", "-c", cmd),
                        shizukuEnv(), "/"
                    )
                    try {
                        p.waitFor()
                    } finally {
                        p.close()
                    }
                }.getOrDefault(-1)
                if (fallback != 0) {
                    withContext(Dispatchers.Main) {
                        softRebooting = false
                        addLog("[soft-reboot] Failed to restart zygote: rc=$fallback")
                        Toast.makeText(this@MainActivity, "Soft reboot failed", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
            }
            withContext(Dispatchers.Main) {
                softRebooting = false
                addLog("[soft-reboot] Framework restarted. Modules applied without losing jailbreak.")
            }
        }
    }

    private fun getBootId(): String {
        return runCatching {
            val f = File("/proc/sys/kernel/random/boot_id")
            if (f.exists() && f.canRead()) f.readText().trim() else null
        }.getOrNull() ?: run {
            val bootSec = (System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()) / 1000
            "boot_$bootSec"
        }
    }

    private fun updateBypassRebootGuard(value: Boolean) {
        bypassRebootGuard = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("bypass_reboot_guard", value).apply()
        if (value) {
            clearBootSessionGuard()
        }
        addLog("Bypass reboot guard: ${if (value) "enabled" else "disabled"}")
    }

    private fun clearBootSessionGuard() {
        getSharedPreferences("pyb110", MODE_PRIVATE)
            .edit()
            .remove("last_run_boot_id")
            .apply()
        lifecycleScope.launch(Dispatchers.Main) {
            bootSessionRan = false
        }
        addLog("[boot-guard] Session lock cleared - re-run unlocked")
    }

    private fun runDoneThisBoot(): Boolean {
        if (bypassRebootGuard || BuildConfig.VERSION_NAME.contains("Test", ignoreCase = true)) return false
        if (bootSessionRan) return true
        val lastBootId = getSharedPreferences("pyb110", MODE_PRIVATE).getString("last_run_boot_id", null)
        val ran = lastBootId != null && lastBootId == getBootId()
        if (ran) bootSessionRan = true
        return ran
    }

    private fun markRunThisBoot() {
        if (bypassRebootGuard || BuildConfig.VERSION_NAME.contains("Test", ignoreCase = true)) {
            addLog("[boot-guard] Test build / bypass active: skipping boot lock")
            return
        }
        getSharedPreferences("pyb110", MODE_PRIVATE)
            .edit()
            .putString("last_run_boot_id", getBootId())
            .apply()
        lifecycleScope.launch(Dispatchers.Main) {
            bootSessionRan = true
        }
    }

    private fun copyDiagnosticReport() {
        val cm = getSystemService(ClipboardManager::class.java)
        val marketName = DeviceNameResolver.getCachedOrLocalDeviceName(this)
        val fullReport = """
            # Shark-Moto Device Diagnostic Report
            Market Name: $marketName
            Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})
            Brand: ${Build.BRAND}
            Fingerprint: ${Build.FINGERPRINT}
            Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}, Patch ${Build.VERSION.SECURITY_PATCH})
            Kernel: $kernelVersion
            SELinux: $selinuxText
            Shark: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            Support Status: ${if (exploit != ExploitKind.UNSUPPORTED) "Supported (${exploit.fullLabel})" else "UNSUPPORTED"}

            --- Shell Diagnostic Output ---
            $diagnosticOutput
        """.trimIndent()
        cm?.setPrimaryClip(ClipData.newPlainText("Shark Diagnostic", fullReport))
        Toast.makeText(this@MainActivity, AppStrings.diagnosticCopied(appLanguage), Toast.LENGTH_SHORT).show()
    }

    private fun submitGithubIssue() {
        copyDiagnosticReport()
        val repo = "CYB3R0ID694/Shark-Moto"
        val marketName = DeviceNameResolver.getCachedOrLocalDeviceName(this)
        val title = "[Device Support] $marketName (${Build.MODEL}) ($kernelVersion)"
        val cleanOutput = diagnosticOutput.take(1200)
        val body = """
            ### Device & Kernel Information
            - **Device**: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})
            - **Android Version**: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            - **Security Patch**: ${Build.VERSION.SECURITY_PATCH}
            - **Kernel (`uname -r`)**: `$kernelVersion`
            - **Fingerprint**: `${Build.FINGERPRINT}`

            ### Diagnostic Output (via Shizuku)
            ```text
            $cleanOutput
            ```

            > [!NOTE]
            > Full diagnostic output was also automatically copied to the clipboard. Please paste below if needed.
        """.trimIndent()

        val url = "https://github.com/$repo/issues/new?title=" +
            java.net.URLEncoder.encode(title, "UTF-8") +
            "&body=" +
            java.net.URLEncoder.encode(body, "UTF-8")

        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this@MainActivity, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTelegramSupport() {
        copyDiagnosticReport()
        val tgUrl = TELEGRAM_GROUP_URL
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tgUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }.onFailure {
            Toast.makeText(this@MainActivity, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDiagnosticReport(customProfileJson: String? = null) {
        if (uploadingReport) return
        uploadingReport = true
        lifecycleScope.launch {
            val marketName = DeviceNameResolver.resolveDeviceNameOnline(applicationContext)
            withContext(Dispatchers.Main) {
                deviceModel = marketName
            }
            var targetDevice = DeviceNameResolver.formatDisplayDevice(marketName)
            var targetBrand = Build.BRAND
            var targetKernel = kernelVersion
            var targetFingerprint = Build.FINGERPRINT

            if (!customProfileJson.isNullOrBlank()) {
                runCatching {
                    val obj = org.json.JSONObject(customProfileJson)
                    val pModel = obj.optString("model")
                    val pRelease = obj.optString("release")
                    if (pModel.isNotBlank()) {
                        targetDevice = pModel
                        val firstWord = pModel.split(" ", "_", "-").firstOrNull().orEmpty()
                        if (firstWord.isNotBlank()) targetBrand = firstWord
                    }
                    if (pRelease.isNotBlank()) {
                        targetKernel = pRelease
                    }
                    targetFingerprint = "Imported from boot.img ($pRelease)"
                }
            }

            val fullReport = """
                Device: $targetDevice
                Model: ${Build.MODEL} (${Build.DEVICE})
                Brand: $targetBrand
                Fingerprint: $targetFingerprint
                Kernel: $targetKernel
                Engine: ${exploit.fullLabel}
                SELinux: $selinuxText
                Backend: ${PrivService.pick()?.label ?: "None"} (authorized=$authorized)
                ${if (diagnosticOutput.isNotBlank()) "Output:\n" + diagnosticOutput.take(1500) else ""}
            """.trimIndent()
            addLog("[support-upload] Uploading device support request to Telegram group...")
            val result = SupportUploader.uploadReport(
                kernelVersion = targetKernel,
                diagnosticReport = fullReport,
                customProfileJson = customProfileJson
            )
            uploadingReport = false
            result.onSuccess {
                addLog("[support-upload] Success: Diagnostic posted to Telegram support group.")
                Toast.makeText(this@MainActivity, AppStrings.submitReportSuccess(appLanguage), Toast.LENGTH_LONG).show()
            }.onFailure {
                addLog("[support-upload] Failed: ${it.message}")
                Toast.makeText(this@MainActivity, AppStrings.submitReportFailed(it.message ?: "unknown", appLanguage), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun runShizukuDiagnostic() {
        if (diagnosticRunning) return
        if (!binderReady || !authorized) {
            Toast.makeText(this, AppStrings.actionAuthorizeShizuku(appLanguage), Toast.LENGTH_SHORT).show()
            return
        }
        diagnosticRunning = true
        diagnosticOutput = AppStrings.actionRunningDiagnostic(appLanguage)
        lifecycleScope.launch(Dispatchers.IO) {
            val marketName = DeviceNameResolver.resolveDeviceNameOnline(applicationContext)
            withContext(Dispatchers.Main) {
                deviceModel = marketName
            }
            val nativeDir = applicationInfo.nativeLibraryDir
            val script = """
                echo "=== SYSTEM & KERNEL BANNER ==="
                echo "Release: $(uname -r)"
                echo "Version: $(cat /proc/version 2>/dev/null || uname -v)"
                echo "Machine: $(uname -m)"
                echo ""
                echo "=== DEVICE BUILD PROPERTIES ==="
                echo "Market Name: $marketName"
                echo "Manufacturer: $(getprop ro.product.manufacturer)"
                echo "Brand: $(getprop ro.product.brand)"
                echo "Model: $(getprop ro.product.model)"
                echo "Device: $(getprop ro.product.device)"
                echo "Board: $(getprop ro.board.platform)"
                echo "Fingerprint: $(getprop ro.build.fingerprint)"
                echo "Android: $(getprop ro.build.version.release) (SDK $(getprop ro.build.version.sdk))"
                echo "Security Patch: $(getprop ro.build.version.security_patch)"
                echo ""
                echo "=== SELINUX & TRACING CONFIG ==="
                echo "SELinux Mode: $(getenforce 2>/dev/null || echo unknown)"
                echo "Tracing buffer_size_kb: $(cat /sys/kernel/tracing/buffer_size_kb 2>/dev/null || echo 'unreadable')"
                echo "Trace events dir: $(if [ -d /sys/kernel/tracing/events ]; then echo 'accessible'; else echo 'restricted'; fi)"
                echo ""
                echo "=== KALLSYMS ACCESSIBILITY ==="
                if [ -r /proc/kallsyms ]; then
                    echo "kallsyms: readable ($(head -n 2 /proc/kallsyms 2>/dev/null | tr '\n' ' '))"
                else
                    echo "kallsyms: restricted (normal for unrooted user)"
                fi
                echo ""
                echo "=== READ-ONLY PROBE TEST ==="
                if [ -x "$nativeDir/libk6689.so" ] && echo "$kernelVersion" | grep -q "6.6.89"; then
                    echo "Executing libk6689.so probe:"
                    "$nativeDir/libk6689.so" probe 2>&1
                elif [ -x "$nativeDir/libk61230.so" ] && echo "$kernelVersion" | grep -q "6.12.30"; then
                    echo "Executing libk61230.so probe:"
                    "$nativeDir/libk61230.so" probe 2>&1
                elif [ -x "$nativeDir/libk66118.so" ] && echo "$kernelVersion" | grep -q "6.6.118"; then
                    echo "Executing libk66118.so probe:"
                    "$nativeDir/libk66118.so" probe 2>&1
                elif [ -x "$nativeDir/libk61258.so" ] && echo "$kernelVersion" | grep -q "6.12.58"; then
                    echo "Executing libk61258.so probe:"
                    "$nativeDir/libk61258.so" probe 2>&1
                elif [ -x "$nativeDir/libk61258x.so" ] && echo "$kernelVersion" | grep -q "6.12.58"; then
                    echo "Executing libk61258x.so probe:"
                    "$nativeDir/libk61258x.so" probe 2>&1
                elif [ -x "$nativeDir/libk61238.so" ] && echo "$kernelVersion" | grep -q "6.12.38"; then
                    echo "Executing libk61238.so probe:"
                    "$nativeDir/libk61238.so" probe 2>&1
                elif [ -x "$nativeDir/libk61269.so" ] && echo "$kernelVersion" | grep -q "6.12.69"; then
                    echo "Executing libk61269.so probe:"
                    "$nativeDir/libk61269.so" probe 2>&1
                elif [ -x "$nativeDir/libk61821p.so" ] && echo "$kernelVersion" | grep -q "6.18.21"; then
                    echo "Executing libk61821p.so probe:"
                    "$nativeDir/libk61821p.so" probe 2>&1
                elif [ -x "$nativeDir/libk61821x.so" ] && echo "$kernelVersion" | grep -q "6.18.21" && echo "$kernelVersion" | grep -q "abogki"; then
                    echo "Executing libk61821x.so probe:"
                    "$nativeDir/libk61821x.so" probe 2>&1
                elif [ -x "$nativeDir/libk61821.so" ] && echo "$kernelVersion" | grep -q "6.18.21"; then
                    echo "Executing libk61821.so probe:"
                    "$nativeDir/libk61821.so" probe 2>&1
                elif [ -x "$nativeDir/libk61.so" ] && echo "$kernelVersion" | grep -q "6.1\."; then
                    echo "Executing libk61.so probe:"
                    "$nativeDir/libk61.so" probe 2>&1
                elif echo "$kernelVersion" | grep -q "6.1\."; then
                    echo "=== LINUX 6.1 SERIES KERNEL DETECTED ==="
                    echo "Release: $kernelVersion"
                    echo "Status: Linux 6.1 (Android 14 GKI) requires dedicated libk61 engine runner."
                    echo "SELinux mode and tracefs parameters above are recorded for 6.1 porting."
                    echo "Please share this diagnostic report on Telegram (@sharkmotosupport)."
                else
                    echo "No matched native engine probe available for this kernel version."
                    echo "Please submit diagnostic output to Telegram or GitHub to request support profile."
                fi
            """.trimIndent()

            val result = runCatching {
                val p = PrivService.newProcess(arrayOf("sh", "-c", script), shizukuEnv(), "/")
                try {
                    var out = ""
                    var err = ""
                    val outThread = Thread {
                        out = runCatching { p.getInputStream().bufferedReader().readText().trim() }.getOrDefault("")
                    }.apply { start() }
                    val errThread = Thread {
                        err = runCatching { p.getErrorStream().bufferedReader().readText().trim() }.getOrDefault("")
                    }.apply { start() }
                    p.waitFor()
                    outThread.join(2000)
                    errThread.join(2000)
                    if (err.isNotEmpty()) "$out\n[STDERR]\n$err" else out
                } finally {
                    p.close()
                }
            }.getOrElse { "Failed to run Shizuku diagnostic: ${it.message}" }

            withContext(Dispatchers.Main) {
                diagnosticOutput = result
                diagnosticRunning = false
            }
        }
    }

    @Composable
    private fun DiagnosticScreen(inner: PaddingValues) {
        val colors = CouiPageDefaults.themeColors()
        val dark = CouiTheme.isDark
        val isSupported = exploit != ExploitKind.UNSUPPORTED
        val statusText = if (isSupported) {
            "${AppStrings.diagnosticStatusSupported(appLanguage)} (${exploit.fullLabel})"
        } else {
            AppStrings.diagnosticStatusUnsupported(appLanguage)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 4.dp,
                bottom = inner.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                Group {
                    SettingItem(
                        icon = Icons.Filled.Smartphone,
                        title = "$deviceModel (${Build.MANUFACTURER})",
                        summary = "Android ${Build.VERSION.RELEASE} (Patch: ${Build.VERSION.SECURITY_PATCH})"
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.DeveloperBoard,
                        title = "Kernel: $kernelVersion",
                        summary = statusText
                    )
                }
            }

            item {
                Group {
                    if (!binderReady) {
                        SettingItem(
                            icon = Icons.Filled.HealthAndSafety,
                            title = AppStrings.actionStellarNotRunning(appLanguage),
                            summary = "Tap to open Shizuku / Stellar manager",
                            onClick = { PrivService.openManager(applicationContext) },
                            trailing = { Chevron() }
                        )
                    } else if (!authorized) {
                        SettingItem(
                            icon = Icons.Filled.HealthAndSafety,
                            title = AppStrings.actionAuthorizeShizuku(appLanguage),
                            summary = "Tap to grant shell permission to Shark",
                            onClick = { PrivService.request(REQ_SHIZUKU) },
                            trailing = { Chevron() }
                        )
                    } else {
                        SettingItem(
                            icon = Icons.Filled.Check,
                            title = "Shizuku Shell Access Granted",
                            summary = "Backend UID 2000 (Ready for inspection)"
                        )
                    }
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = if (diagnosticRunning) Icons.Filled.Schedule else Icons.Filled.Refresh,
                        title = if (diagnosticRunning) AppStrings.actionRunningDiagnostic(appLanguage) else AppStrings.actionRunDiagnostic(appLanguage),
                        summary = "Inspect kernel, KASLR alignment, tracing & run read-only probe",
                        onClick = { runShizukuDiagnostic() },
                        enabled = !diagnosticRunning && binderReady && authorized,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Download,
                        title = if (syncingProfiles) AppStrings.actionRunningDiagnostic(appLanguage) else AppStrings.syncProfiles(appLanguage),
                        summary = "${AppStrings.syncProfilesSummary(appLanguage)} (${dynamicProfileCount} active)",
                        onClick = { syncProfilesOnline() },
                        enabled = !syncingProfiles && !syncingGhostLock,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Refresh,
                        title = if (syncingGhostLock) AppStrings.actionRunningDiagnostic(appLanguage) else AppStrings.syncGhostLockTitle(appLanguage),
                        summary = AppStrings.syncGhostLockSummary(appLanguage),
                        onClick = { syncGhostLockProfilesOnline() },
                        enabled = !syncingProfiles && !syncingGhostLock,
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Memory,
                        title = if (parsingBoot) AppStrings.parsingBootTitle(appLanguage) else AppStrings.parseBootImage(appLanguage),
                        summary = AppStrings.parseBootImageSummary(appLanguage),
                        onClick = { launchBootPicker() },
                        enabled = !parsingBoot,
                        trailing = { Chevron() }
                    )
                }
            }

            if (diagnosticOutput.isNotEmpty()) {
                item {
                    Group {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Diagnostic Console",
                                    kind = CouiTextKind.SectionTitle,
                                    color = colors.title
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accent.copy(alpha = 0.1f))
                                        .clickable { copyDiagnosticReport() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = AppStrings.actionCopyDiagnostic(appLanguage),
                                        kind = CouiTextKind.Category,
                                        color = colors.accent
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            SelectionContainer {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (dark) Color(0xFF121212) else Color(0xFFF1F3F4))
                                        .padding(12.dp)
                                ) {
                                    BasicText(
                                        text = diagnosticOutput,
                                        style = rememberCouiTextStyle(CouiTextKind.Body).copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = if (dark) Color(0xFF00E676) else Color(0xFF007A33)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Group {
                        SettingItem(
                            icon = Icons.Filled.ContentCopy,
                            title = AppStrings.actionCopyDiagnostic(appLanguage),
                            summary = "Copy full log output to clipboard",
                            onClick = { copyDiagnosticReport() }
                        )
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingItem(
                            icon = Icons.Filled.Forum,
                            title = AppStrings.actionSubmitGithubIssue(appLanguage),
                            summary = "Generate and open pre-filled issue on CYB3R0ID694/Shark-Moto",
                            onClick = { submitGithubIssue() },
                            trailing = { Chevron() }
                        )
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingItem(
                            icon = Icons.Filled.Share,
                            title = if (uploadingReport) AppStrings.submittingReport(appLanguage) else AppStrings.submitDeviceForSupport(appLanguage),
                            summary = "Direct upload to Telegram support group (@sharkmotosupport)",
                            onClick = { uploadDiagnosticReport() },
                            enabled = !uploadingReport,
                            trailing = { Chevron() }
                        )
                        Divider(startIndent = itemDividerIndent(), color = colors.divider)
                        SettingItem(
                            icon = Icons.Filled.Share,
                            title = AppStrings.actionJoinTelegramSupport(appLanguage),
                            summary = "https://t.me/sharkmotosupport (Direct community help)",
                            onClick = { openTelegramSupport() },
                            trailing = { Chevron() }
                        )
                    }
                }
            }
        }
    }

    /* ===== Supported Devices ===== */

    private data class SupportedDeviceEntry(
        val brand: String,
        val name: String,
        val kernels: List<String>
    )

    private fun getSupportedDevicesList(): List<SupportedDeviceEntry> {
        val staticList = listOf(
            SupportedDeviceEntry(
                brand = "Motorola",
                name = "Motorola Edge 70 Pro",
                kernels = listOf("6.6.89-android15-8-gdcee9aa4fcbc-ab14676413-4k")
            ),
            SupportedDeviceEntry(
                brand = "Motorola",
                name = "Motorola Signature",
                kernels = listOf(
                    "6.12.58-android16-6-gb0a9599c6c01-ab14987696-4k",
                    "6.12.58-android16-6-g09eda8c8505a-ab15950084-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "POCO / Xiaomi",
                name = "Xiaomi POCO M7 Pro (beryl)",
                kernels = listOf("6.12.30-android16-5-g1ed949324a3e-ab13881345-4k")
            ),
            SupportedDeviceEntry(
                brand = "Redmi / Xiaomi",
                name = "Redmi Turbo 4 Pro (25053RT47C / onyx)",
                kernels = listOf("6.6.118-android15-8-g0908354dac1b-ab16143355-4k")
            ),
            SupportedDeviceEntry(
                brand = "OnePlus",
                name = "OnePlus 16 (PYB110)",
                kernels = listOf(
                    "6.18.21-android17-5-gf957accaf563-ab15926176-4k",
                    "6.18.21-android17-5-g7fcbffcad3e1-ab16353812-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OnePlus",
                name = "OnePlus 15 / 15T / Ace7 Pro / Ace7 / Ace6 Ultra",
                kernels = listOf(
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OnePlus",
                name = "OnePlus Ace6T (PLR110)",
                kernels = listOf(
                    "6.12.38-android16-5-g844001fb8721-ab14552068-4k",
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OnePlus",
                name = "OnePlus Ace6 (PLQ110) / Ace5 Pro (PKR110)",
                kernels = listOf("6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k")
            ),
            SupportedDeviceEntry(
                brand = "OnePlus",
                name = "OnePlus Pad3 Pro / Pad2 Pro",
                kernels = listOf(
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k",
                    "6.12.58-android16-6-g7704a1ae279b-ab15213644-4k",
                    "6.6.118-android15-8-gc4127a25dcf3-ab15863337-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OPPO",
                name = "OPPO Find X9 / X9 Pro / X9 Ultra / X9s Pro / X10",
                kernels = listOf(
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OPPO",
                name = "OPPO Find X8 Ultra / Find N5",
                kernels = listOf(
                    "6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k",
                    "6.6.118-android15-8-gc4127a25dcf3-ab15863337-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OPPO",
                name = "OPPO K14 5G (CPH2869)",
                kernels = listOf(
                    "6.18.21-android17-5-o-g96f274a16f90-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "OPPO",
                name = "OPPO Pad 5 Pro / Pad Mini / Pad4 Pro",
                kernels = listOf(
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k",
                    "6.12.58-android16-6-g7704a1ae279b-ab15213644-4k",
                    "6.6.118-android15-8-gc4127a25dcf3-ab15863337-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "Realme",
                name = "Realme GT8 / GT7 Pro / Neo8 / 12 Pro+ (RMX3867)",
                kernels = listOf(
                    "6.12.69-android16-6-g242e1a07f878-ab15865446-4k",
                    "6.12.69-android16-6-g5fd39602326e-ab16372235-4k",
                    "6.12.38-android16-5-g844001fb8721-ab14552068-4k",
                    "6.6.118-android15-8-g9bc34d5b0c79-abogki537459655-4k",
                    "6.6.118-android15-8-o-ga8027a191504-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "Realme",
                name = "Realme NARZO 70 Turbo 5G (RMX5003)",
                kernels = listOf(
                    "6.1.128-android14-11-o-g9f6b860b6402"
                )
            ),
            SupportedDeviceEntry(
                brand = "iQOO / Vivo",
                name = "iQOO 15 (V2505A) / Ultra / Pad Pro",
                kernels = listOf(
                    "6.12.58-android16-6-g690108013dcc-abogki576129896-4k",
                    "6.12.58-android16-6-g0a092cc0037a-abogki521987229-4k",
                    "6.12.58-android16-6-gdd6fd5c98918-abogki543627742-4k",
                    "6.12.23-android16-5-g41f4cb1fa7c7-abogki465038634-4k",
                    "6.6.127-android15-8-gd489d4ad2a70-abogki567717946-4k"
                )
            ),
            SupportedDeviceEntry(
                brand = "Vivo",
                name = "Vivo X300 Pro (V2514)",
                kernels = listOf(
                    "6.12.58-android16-6-g09eda8c8505a-ab15950084-4k"
                )
            )
        )
        val knownKernels = staticList.flatMap { it.kernels }.toSet()
        val dyn = DynamicProfileManager.allProfiles().filter { it.release !in knownKernels }
        if (dyn.isEmpty()) return staticList
        val extra = dyn.groupBy { it.model }.map { (model, profs) ->
            SupportedDeviceEntry("Dynamic Profile", model, profs.map { it.release })
        }
        return staticList + extra
    }

    @Composable
    private fun DevicesScreen(inner: PaddingValues) {
        val colors = CouiPageDefaults.themeColors()
        val scheme = CouiTheme.colorScheme
        val cm = getSystemService(ClipboardManager::class.java)
        val devices = remember { getSupportedDevicesList() }
        var searchQuery by remember { mutableStateOf("") }
        val isSupported = exploit != ExploitKind.UNSUPPORTED

        val filteredDevices = remember(searchQuery, devices) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) devices
            else devices.filter { dev ->
                dev.name.lowercase().contains(q) ||
                    dev.brand.lowercase().contains(q) ||
                    dev.kernels.any { it.lowercase().contains(q) }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 8.dp,
                bottom = inner.calculateBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Group {
                    SettingItem(
                        icon = if (isSupported) Icons.Filled.Check else Icons.Filled.Info,
                        title = "${AppStrings.thisDeviceHeader(appLanguage)}: ${JailbreakCore.resolveDeviceName()}",
                        summary = if (isSupported) {
                            "${AppStrings.thisDeviceStatusSupported(exploit.fullLabel, appLanguage)}\n$kernelVersion"
                        } else {
                            "${AppStrings.thisDeviceStatusUnsupported(appLanguage)}\n$kernelVersion"
                        },
                        onClick = if (!isSupported) {
                            { showDiagnostic = true }
                        } else null
                    )
                }
            }

            item {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(scheme.card, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = TextStyle(color = scheme.onSurface, fontSize = 14.sp),
                    singleLine = true,
                    decorationBox = { innerField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(
                                icon = Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = AppStrings.searchDevicesPlaceholder(appLanguage),
                                        kind = CouiTextKind.Description,
                                        color = scheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { searchQuery = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AppIcon(
                                        icon = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = scheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            item {
                Text(
                    text = AppStrings.supportedDevicesCount(filteredDevices.size, appLanguage),
                    kind = CouiTextKind.Category,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            if (filteredDevices.isEmpty()) {
                item {
                    Text(
                        text = AppStrings.noDevicesFound(appLanguage),
                        kind = CouiTextKind.Description,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            } else {
                items(filteredDevices) { dev ->
                    Group {
                        SettingItem(
                            icon = Icons.Filled.Smartphone,
                            title = dev.name,
                            summary = "${dev.brand} • ${dev.kernels.size} kernel build(s)"
                        )
                        dev.kernels.forEach { k ->
                            val isMatched = k == kernelVersion
                            Divider(startIndent = itemDividerIndent(), color = colors.divider)
                            SettingItem(
                                icon = Icons.Filled.DeveloperBoard,
                                title = k,
                                summary = if (isMatched) AppStrings.activeKernelMatch(appLanguage) else "Tap to copy kernel string",
                                onClick = {
                                    cm?.setPrimaryClip(ClipData.newPlainText("Kernel", k))
                                    Toast.makeText(this@MainActivity, AppStrings.copiedKernel(appLanguage), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /* ===== Manual ===== */

    @Composable
    private fun ManualScreen(inner: PaddingValues) {
        val context = LocalContext.current
        val markdown = remember(appLanguage) {
            val assetName = if (appLanguage == AppLanguage.ZH) "manual_zh.md" else "manual.md"
            runCatching {
                context.assets.open(assetName).bufferedReader().use { it.readText() }
            }.getOrElse { "# Manual\n\nFailed to load manual content." }
        }
        val blocks = remember(markdown) { parseManualMarkdown(markdown) }
        val codeBackground = CouiTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 16.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = inner.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(blocks) { _, block ->
                MdBlockView(block, codeBackground)
            }
        }
    }

    /* ===== Logs ===== */

@Composable
    private fun LogScreen(inner: PaddingValues) {
        val scheme = CouiTheme.colorScheme
        val context = LocalContext.current
        val state = logs.toList()
        var filterIndex by remember { mutableStateOf(0) }

        val filteredLogs = remember(state, filterIndex) {
            when (filterIndex) {
                1 -> state.filter { line ->
                    line.contains("FAIL", ignoreCase = true) || line.contains("error", ignoreCase = true) ||
                        line.contains("mismatch", ignoreCase = true) || line.contains("exit=", ignoreCase = true)
                }
                2 -> state.filter { line ->
                    line.contains("PROBE", ignoreCase = true) || line.contains("PLANT", ignoreCase = true) ||
                        line.contains("EXPLOIT", ignoreCase = true)
                }
                3 -> state.filter { line ->
                    line.contains("KernelSU", ignoreCase = true) || line.contains("ksud", ignoreCase = true)
                }
                else -> state
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = inner.calculateTopPadding() + 4.dp,
                    bottom = inner.calculateBottomPadding() + 8.dp
                )
        ) {
            // Action Bar: Copy All, Share, Clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surface)
                        .clickable(enabled = state.isNotEmpty()) {
                            val cm = context.getSystemService(ClipboardManager::class.java)
                            cm?.setPrimaryClip(ClipData.newPlainText("Shark Logs", state.joinToString("\n")))
                            Toast.makeText(context, AppStrings.logsCopied(appLanguage), Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Filled.ContentCopy, null, Modifier.size(14.dp), tint = scheme.accent)
                        Spacer(Modifier.width(4.dp))
                        Text(text = AppStrings.copyLogs(appLanguage), kind = CouiTextKind.Category, color = scheme.accent)
                    }
                }

                // Share Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surface)
                        .clickable(enabled = state.isNotEmpty()) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, state.joinToString("\n"))
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, AppStrings.shareLogs(appLanguage)))
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Filled.Share, null, Modifier.size(14.dp), tint = scheme.accent)
                        Spacer(Modifier.width(4.dp))
                        Text(text = AppStrings.shareLogs(appLanguage), kind = CouiTextKind.Category, color = scheme.accent)
                    }
                }

                Spacer(Modifier.weight(1f))

                // Clear Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surface)
                        .clickable(enabled = state.isNotEmpty()) {
                            logs.clear()
                            Toast.makeText(context, AppStrings.logsCleared(appLanguage), Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(Icons.Filled.DeleteOutline, null, Modifier.size(14.dp), tint = Color(0xFFE57373))
                        Spacer(Modifier.width(4.dp))
                        Text(text = AppStrings.clearLogs(appLanguage), kind = CouiTextKind.Category, color = Color(0xFFE57373))
                    }
                }
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chips = listOf(
                    AppStrings.filterAll(appLanguage),
                    AppStrings.filterErrors(appLanguage),
                    AppStrings.filterProbe(appLanguage),
                    AppStrings.filterKsu(appLanguage)
                )
                chips.forEachIndexed { idx, label ->
                    val isSel = filterIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) scheme.accent else scheme.surface)
                            .clickable { filterIndex = idx }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            kind = CouiTextKind.Category,
                            color = if (isSel) Color.White else scheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.noLogs(appLanguage),
                        kind = CouiTextKind.Body,
                        color = scheme.onSurfaceVariant
                    )
                }
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(filteredLogs.size) {
                    if (filteredLogs.isNotEmpty()) {
                        listState.scrollToItem(filteredLogs.lastIndex)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredLogs) { line ->
                        val lineColor = when {
                            line.contains("FAIL", ignoreCase = true) || line.contains("error", ignoreCase = true) || line.contains("mismatch", ignoreCase = true) -> Color(0xFFFF5252)
                            line.contains("SUCCESS", ignoreCase = true) || line.contains("ok", ignoreCase = true) || line.contains("hit", ignoreCase = true) || line.contains("RESULT=OK") -> Color(0xFF66BB6A)
                            line.contains("PROBE", ignoreCase = true) || line.contains("PLANT", ignoreCase = true) || line.contains("EXPLOIT", ignoreCase = true) -> Color(0xFF26C6DA)
                            line.contains("START", ignoreCase = true) || line.contains("BOOT_REASON") -> Color(0xFFFFB74D)
                            else -> scheme.onSurface
                        }
                        BasicText(
                            text = line,
                            style = rememberCouiTextStyle(CouiTextKind.Description).copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = lineColor
                            )
                        )
                    }
                }
            }
        }
    }

    /* ===== History ===== */

    @Composable
    private fun HistoryScreen(inner: PaddingValues) {
        LaunchedEffect(Unit) { refreshHistory() }
        val entries = historyEntries
        val scheme = CouiTheme.colorScheme
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.ZH) "暂无历史记录" else "No history yet",
                    kind = CouiTextKind.Body,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + 4.dp,
                    bottom = inner.calculateBottomPadding() + 12.dp
                )
            ) {
                items(entries) { e ->
                    HistoryEntryCard(
                        entry = e,
                        onClick = {
                            val name = e.fileName
                            lifecycleScope.launch(Dispatchers.IO) {
                                val text = HistoryStore.read(applicationContext, name)
                                withContext(Dispatchers.Main) {
                                    historyDetailText = text
                                    historyDetail = e
                                }
                            }
                        },
                        onExport = { shareHistory(e) }
                    )
                }
            }
        }
        HistoryDetailDialog(
            entry = historyDetail,
            text = historyDetailText,
            onDismiss = { historyDetail = null },
            onDelete = {
                val name = historyDetail?.fileName
                if (name != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        HistoryStore.delete(applicationContext, name)
                        withContext(Dispatchers.Main) {
                            historyDetail = null
                            refreshHistory()
                        }
                    }
                }
            }
        )
    }

    /* 条目右侧滑出导出操作；卡片本体使用 COUI 卡片配色与列表按压反馈。 */
    @Composable
    private fun HistoryEntryCard(entry: HistoryEntry, onClick: () -> Unit, onExport: () -> Unit) {
        val scheme = CouiTheme.colorScheme
        val colors = CouiPageDefaults.themeColors()
        val dark = CouiTheme.isDark
        val scope = rememberCoroutineScope()
        val offset = remember { Animatable(0f) }
        val density = LocalDensity.current
        val actionWidth = with(density) { 88.dp.toPx() }
        val source = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            val reveal = (-offset.value).coerceIn(0f, actionWidth)
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(with(density) { reveal.toDp() })
                        .fillMaxHeight()
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(88.dp)
                            .fillMaxHeight()
                            .clip(CouiPageDefaults.shape)
                            .background(scheme.accent)
                            .clickable {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                                onExport()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Export", kind = CouiTextKind.PreferenceTitle, color = scheme.onAccent)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = offset.value }
                    .clip(CouiPageDefaults.shape)
                    .background(colors.card)
                    .couiButtonFeedback(dark, source, list = true)
                    .clickable {
                        if (offset.value < -1f) {
                            scope.launch { offset.animateTo(0f, tween(200)) }
                        } else {
                            onClick()
                        }
                    }
                    .pointerInput(entry.fileName) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val target = if (offset.value < -actionWidth / 2f) -actionWidth else 0f
                                scope.launch { offset.animateTo(target, tween(200)) }
                            },
                            onDragCancel = {
                                scope.launch { offset.animateTo(0f, tween(200)) }
                            },
                            onHorizontalDrag = { _, delta ->
                                scope.launch {
                                    offset.snapTo((offset.value + delta).coerceIn(-actionWidth, 0f))
                                }
                            }
                        )
                    }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = entry.manager,
                            kind = CouiTextKind.PreferenceTitle,
                            color = colors.title,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = entry.exploit,
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = entry.startTime,
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.basicMarquee()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val tagColors = listOf(
                                scheme.accent to scheme.onAccent,
                                scheme.accent.copy(alpha = 0.14f) to scheme.accent,
                                scheme.onSurface.copy(alpha = 0.08f) to scheme.onSurfaceVariant
                            )
                            historyTags(entry).forEachIndexed { index, tag ->
                                val (bg, fg) = tagColors.getOrElse(index) { tagColors.last() }
                                CouiStatusTag(label = tag, backgroundColor = bg, contentColor = fg)
                            }
                        }
                    }
                    Text(
                        text = historyValue(entry),
                        kind = CouiTextKind.PreferenceSummary,
                        color = colors.summary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    AppIcon(
                        CouiIcons.Next,
                        null,
                        Modifier.padding(start = 4.dp).size(18.dp),
                        tint = colors.summary
                    )
                }
            }
        }
    }

    @Composable
    private fun CouiStatusTag(label: String, backgroundColor: Color, contentColor: Color) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                kind = CouiTextKind.Category,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }

    @Composable
    private fun HistoryDetailDialog(
        entry: HistoryEntry?,
        text: String,
        onDismiss: () -> Unit,
        onDelete: () -> Unit
    ) {
        var lastEntry by remember { mutableStateOf(entry) }
        if (entry != null) lastEntry = entry
        val displayEntry = lastEntry ?: return
        val scheme = CouiTheme.colorScheme
        Dialog(
            onDismissRequest = onDismiss,
            title = "${displayEntry.startTime}  ${displayEntry.manager}",
            confirmAction = CouiDialogAction(
                text = "OK",
                onClick = onDismiss,
                style = CouiDialogActionStyle.Primary,
                colors = CouiDialogDefaults.primaryActionColors(scheme.accent)
            ),
            dismissAction = CouiDialogAction(text = "Delete", onClick = onDelete),
            visible = entry != null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CouiDialogDefaults.contentPadding)
            ) {
                SelectionContainer {
                    Box(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                        BasicText(
                            text = text.ifEmpty { "No log content" },
                            style = rememberCouiTextStyle(CouiTextKind.Body).copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = scheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }

    private fun historyTags(e: HistoryEntry): List<String> = listOfNotNull(
        e.model.takeIf { it.isNotBlank() },
        e.kernel.takeIf { it.isNotBlank() }?.substringBefore('-')
    )

    /* Right-swipe export: copy the run log to cache/export and open the
     * system share sheet through FileProvider. */
    private fun shareHistory(entry: HistoryEntry) {
        lifecycleScope.launch(Dispatchers.IO) {
            val text = HistoryStore.read(applicationContext, entry.fileName)
            val dir = File(cacheDir, "export").apply { mkdirs() }
            val out = File(dir, entry.fileName)
            out.writeText(text)
            val uri = runCatching {
                FileProvider.getUriForFile(
                    applicationContext,
                    "$packageName.fileprovider",
                    out
                )
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (uri == null) {
                    addLog("Failed to prepare log for sharing")
                    return@withContext
                }
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Shark log ${entry.startTime}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching {
                    startActivity(Intent.createChooser(send, "Export Log"))
                }.onFailure { addLog("Failed to open share sheet: $it") }
            }
        }
    }

    private fun historyValue(e: HistoryEntry): String {
        val dur = e.durationMs?.let { String.format(java.util.Locale.US, "%.1fs", it / 1000.0) }
        val isZh = appLanguage == AppLanguage.ZH
        return when {
            e.result == "SUCCESS" && dur != null -> if (isZh) "成功 $dur" else "Success $dur"
            e.result == "SUCCESS" -> if (isZh) "成功" else "Success"
            e.result == "FAIL" && dur != null -> if (isZh) "失败 $dur" else "Failed $dur"
            e.result == "FAIL" -> if (isZh) "失败" else "Failed"
            else -> if (isZh) "进行中" else "Running"
        }
    }

    /* ===== About ===== */

    @Composable
    private fun AboutScreen(inner: PaddingValues) {
        AnimatedContent(
            targetState = aboutPage,
            transitionSpec = {
                if (targetState == AboutPage.LICENSES) {
                    (slideInHorizontally(tween(240)) { it } + fadeIn(tween(240))) togetherWith
                        (slideOutHorizontally(tween(240)) { -it / 4 } + fadeOut(tween(240)))
                } else {
                    (slideInHorizontally(tween(240)) { -it / 4 } + fadeIn(tween(240))) togetherWith
                        (slideOutHorizontally(tween(240)) { it } + fadeOut(tween(240)))
                }
            },
            label = "aboutPage"
        ) { current ->
            if (current == AboutPage.HOME) {
                AboutHomeScreen(inner)
            } else {
                LicenseListScreen(inner)
            }
        }
    }

    @Composable
    private fun AboutHomeScreen(inner: PaddingValues) {
        val uriHandler = LocalUriHandler.current
        val colors = CouiPageDefaults.themeColors()
        val scheme = CouiTheme.colorScheme
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 4.dp,
                bottom = inner.calculateBottomPadding() + 12.dp
            )
        ) {
            item {
                Group {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.mipmap.ic_launcher_foreground),
                                contentDescription = "Shark Neo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }
                        Text(
                            text = "Shark Neo",
                            kind = CouiTextKind.SectionTitle,
                            color = colors.title
                        )
                        Text(
                            text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary
                        )
                    }
                }
            }
            item {
                Group {
                    Item(
                        icon = {
                            AppIcon(Icons.Filled.Info, null, Modifier.size(20.dp), tint = scheme.accent)
                        },
                        onClick = { aboutPage = AboutPage.LICENSES }
                    ) {
                        Text(
                            text = AppStrings.aboutIconNotice(appLanguage),
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.title,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
            item {
                Group {
                    SettingItem(
                        icon = Icons.Filled.Language,
                        title = AppStrings.appLanguage(appLanguage),
                        summary = appLanguage.nativeName,
                        onClick = { showLanguageDialog = true },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Code,
                        title = AppStrings.sourceCode(appLanguage),
                        summary = AppStrings.sourceCodeSummary(appLanguage),
                        onClick = { uriHandler.openUri(PROJECT_URL) },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Forum,
                        title = AppStrings.community(appLanguage),
                        summary = AppStrings.communitySummary(appLanguage),
                        onClick = { uriHandler.openUri(TELEGRAM_GROUP_URL) },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.BugReport,
                        title = AppStrings.reportIssues(appLanguage),
                        summary = AppStrings.reportIssuesSummary(appLanguage),
                        onClick = { uriHandler.openUri("$PROJECT_URL/issues") },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Refresh,
                        title = AppStrings.checkForUpdates(appLanguage),
                        summary = "${AppStrings.currentVersionPrefix(appLanguage)}${BuildConfig.VERSION_NAME}",
                        onClick = { checkForUpdatesManually() },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Favorite,
                        title = AppStrings.supportDeveloper(appLanguage),
                        summary = AppStrings.supportDeveloperSummary(appLanguage),
                        onClick = { showDonate = true },
                        trailing = { Chevron() }
                    )
                    Divider(startIndent = itemDividerIndent(), color = colors.divider)
                    SettingItem(
                        icon = Icons.Filled.Copyright,
                        title = AppStrings.openSourceLicenses(appLanguage),
                        summary = AppStrings.openSourceLicensesSummary(appLanguage),
                        onClick = { aboutPage = AboutPage.LICENSES },
                        trailing = { Chevron() }
                    )
                }
            }
        }
    }

    @Composable
    private fun LicenseListScreen(inner: PaddingValues) {
        val context = LocalContext.current
        val libs by produceState(initialValue = Libs(emptyList(), emptySet()), context) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    val json = context.resources
                        .openRawResource(R.raw.aboutlibraries)
                        .bufferedReader()
                        .use { it.readText() }
                    Libs.Builder().withJson(json).build()
                }.getOrElse { Libs(emptyList(), emptySet()) }
            }
        }
        val localItems = remember {
            localLicenses.map { entry ->
                LicenseItem(
                    name = entry.title,
                    author = entry.summary,
                    website = entry.url,
                    licenses = listOf(
                        LicenseText(entry.licenseName, entry.url, asset = entry.asset)
                    )
                )
            }
        }
        val libraryItems = remember(libs) { libs.libraries.map { it.toLicenseItem() } }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 4.dp,
                bottom = inner.calculateBottomPadding() + 12.dp
            )
        ) {
            item { SectionLabel(AppStrings.projectAndBundled(appLanguage)) }
            items(localItems) { item ->
                LicenseCard(item) { aboutLicense = item }
            }
            item {
                SectionLabel(AppStrings.thirdPartyDeps(libraryItems.size, appLanguage))
            }
            items(libraryItems) { item ->
                LicenseCard(item) { aboutLicense = item }
            }
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        val colors = CouiPageDefaults.themeColors()
        Text(
            text = text,
            kind = CouiTextKind.GroupTitle,
            color = colors.summary,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 12.dp, bottom = 4.dp)
        )
    }

    @Composable
    private fun LicenseChip(text: String) {
        val scheme = CouiTheme.colorScheme
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(scheme.accent.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(text = text, kind = CouiTextKind.Category, color = scheme.accent)
        }
    }

    @Composable
    private fun LicenseCard(item: LicenseItem, onClick: () -> Unit) {
        val colors = CouiPageDefaults.themeColors()
        val hasSummary = !item.author.isNullOrBlank() || item.licenses.isNotEmpty()
        Group {
            Item(
                onClick = onClick,
                summary = if (!hasSummary) null else {
                    {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            item.author?.takeIf { it.isNotBlank() }?.let { author ->
                                Text(
                                    text = author,
                                    kind = CouiTextKind.PreferenceSummary,
                                    color = colors.summary,
                                    textAlign = TextAlign.Start
                                )
                            }
                            if (item.licenses.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item.licenses.take(3).forEach { LicenseChip(it.name) }
                                }
                            }
                        }
                    }
                },
                trailing = {
                    item.version?.takeIf { it.isNotBlank() }?.let { version ->
                        Text(
                            text = version,
                            kind = CouiTextKind.PreferenceSummary,
                            color = colors.summary,
                            maxLines = 1
                        )
                    }
                }
            ) {
                Text(
                    text = item.name,
                    kind = CouiTextKind.PreferenceTitle,
                    color = colors.title,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
    private fun addLog(line: String) {
        Log.i("PYB110", line)
        val stamped = "${timeFmt.format(java.util.Date())}  $line"
        HistoryStore.append(stamped)
        runOnUiThread {
            logs.add(stamped)
            if (logs.size > 4000) logs.removeRange(0, logs.size - 4000)
        }
    }

    private fun refreshHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            val entries = HistoryStore.list(applicationContext)
            withContext(Dispatchers.Main) { historyEntries = entries }
        }
    }

    private fun refreshAuth() {
        authorized = PrivService.granted()
    }

    private fun refreshKsuAsync() {
        if (PrivService.pick() == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val (active, se) = readDeviceState()
                withContext(Dispatchers.Main) {
                    ksuActive = active
                    selinuxText = se
                }
            } catch (t: Throwable) {
                // binder 未就绪时静默
            }
        }
    }

    private fun readDeviceState(): Pair<Boolean, String> {
        val p = PrivService.newProcess(
            arrayOf("sh", "-c", "if grep -q '^kernelsu .* Live ' /proc/modules; then echo YES; elif timeout 3 su -c id 2>/dev/null | grep -q uid=0; then echo YES; else echo NO; fi; getenforce"),
            shizukuEnv(), "/"
        )
        return try {
            val lines = p.getInputStream()
                .bufferedReader().readLines()
            p.waitFor()
            val active = lines.getOrNull(0)?.trim() == "YES"
            val se = mapSelinux(lines.getOrNull(1)?.trim().orEmpty())
            active to se
        } finally {
            p.close()
        }
    }

    private fun updateAntiRootGuard(value: Boolean) {
        antiRootGuard = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("antiroot_guard", value).apply()
        addLog("Anti-root guard: ${if (value) "on (stop riskdetect HAL; restored on reboot)" else "off"}")
    }

    private fun updateSoftRebootAfter(value: Boolean) {
        softRebootAfter = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("soft_reboot_after", value).apply()
        if (!value) {
            softRebootJob?.cancel()
            softRebootJob = null
        }
        addLog("Soft reboot after jailbreak: ${if (value) "on" else "off"}")
    }

    private fun updateSoftRebootDelay(minutes: Int) {
        softRebootDelayMin = minutes
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putInt("soft_reboot_delay_min", minutes).apply()
        addLog("Soft reboot time: ${softRebootDelayLabel(minutes)}")
    }

    private fun softRebootDelayLabel(minutes: Int): String = when (minutes) {
        0 -> "立即"
        1 -> "1Min后"
        2 -> "2Min后"
        3 -> "3Min后"
        else -> "${minutes}Min后"
    }

    private fun updateUnloadGuard(value: Boolean) {
        unloadGuard = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("unload_guard", value).apply()
        addLog("Unload kernel security guard after jailbreak: ${if (value) "on" else "off"}")
    }

    private fun updateSusfsSupport(value: Boolean) {
        susfsSupport = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("susfs_support", value).apply()
        addLog("SUSFS support: ${if (value) "on (load ko after jailbreak)" else "off"}")
    }

    private fun updateVfsSupport(value: Boolean) {
        vfsSupport = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("vfs_support", value).apply()
        addLog("VFS support: ${if (value) "on (load hybridmount ko after jailbreak)" else "off"}")
    }

    private fun updateChaseRetry(value: Boolean) {
        chaseRetry = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("chase_retry", value).apply()
        addLog("Chase retry: ${if (value) "on (retry a missed victim page)" else "off"}")
    }

    private fun updateAutoJailbreak(value: Boolean) {
        autoJailbreak = value
        getSharedPreferences("pyb110", MODE_PRIVATE).edit().putBoolean("auto_jailbreak", value).apply()
        addLog("Boot auto-jailbreak: ${if (value) "on" else "off"}")
        if (!value) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIFICATIONS)
        }
        val power = getSystemService(PowerManager::class.java)
        if (!power.isIgnoringBatteryOptimizations(packageName)) {
            runCatching {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            }.onFailure { addLog("Battery optimization request failed: $it") }
        }
        if (Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)) {
            AlertDialog.Builder(this)
                .setTitle("自启动权限")
                .setMessage("请在 MIUI/HyperOS 的自启动管理中允许 Shark 自启动，否则重启后无法自动越狱。")
                .setPositiveButton("去设置") { _, _ ->
                    if (!openAutoStartSettings()) {
                        Toast.makeText(
                            this, "未找到自启动设置，请手动在系统设置中允许", Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("稍后", null)
                .show()
        }
    }

    private fun openAutoStartSettings(): Boolean {
        val candidates = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)
        )
        for (intent in candidates) {
            if (runCatching { startActivity(intent); true }.getOrDefault(false)) return true
        }
        return false
    }

    private fun selectManager(kind: ManagerKind) {
        manager = kind
        dynamicSwitching = kind == ManagerKind.DYNAMIC
        if (dynamicSwitching) {
            hotManager = ManagerKind.KERNELSU
        }
        getSharedPreferences("pyb110", MODE_PRIVATE).edit()
            .putString("manager", kind.name)
            .putBoolean("dynamic_switching", dynamicSwitching)
            .apply()
        addLog(
            if (dynamicSwitching) "ROOT manager selected: Dynamic Switching (universal LKM, initial KernelSU)"
            else "ROOT manager selected: ${kind.fullLabel}"
        )
    }

    /**
     * Prefer the App's own KernelSU grant; fall back to Shizuku/Stellar.
     * The hot-switch card must not disappear just because the privileged
     * backend is not running while the App itself already has root.
     */
    private fun runRootCommand(script: String, timeoutSeconds: Long = 8): Pair<Int, String> {
        var directFailure: Pair<Int, String>? = null
        runCatching {
            val p = ProcessBuilder("/system/bin/su", "-c", script).redirectErrorStream(true).start()
            var outText = ""
            val readerThread = Thread {
                outText = runCatching { p.inputStream.bufferedReader().readText().trim() }.getOrDefault("")
            }.apply { start() }
            if (p.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                readerThread.join(1000)
                val result = p.exitValue() to outText
                if (result.first == 0) return result
                directFailure = result
            } else {
                p.destroyForcibly()
                readerThread.interrupt()
                directFailure = -2 to "su timeout"
            }
        }
        val fallback = runCatching {
            val p = PrivService.newProcess(arrayOf("su", "-c", script), shizukuEnv(), "/")
            try {
                val out = p.getInputStream().bufferedReader().readText().trim()
                p.waitFor() to out
            } finally {
                p.close()
            }
        }.getOrElse { -1 to (it.message ?: "root command failed") }
        return if (fallback.first == 0) fallback else (directFailure ?: fallback)
    }

    private fun switchHotManager(kind: ManagerKind) {
        if (hotSwitchBusy || kind == ManagerKind.DYNAMIC) return
        hotSwitchBusy = true
        lifecycleScope.launch(Dispatchers.IO) {
            val pkg = resolveInstalledManagerPkg(kind)
            if (!pkg.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                withContext(Dispatchers.Main) {
                    hotSwitchBusy = false
                    addLog("Hot switch failed: invalid package name $pkg")
                }
                return@launch
            }
            val appInfo = runCatching {
                packageManager.getPackageInfo(pkg, 0).applicationInfo
            }.getOrNull()
            if (appInfo == null) {
                withContext(Dispatchers.Main) {
                    hotSwitchBusy = false
                    addLog("Hot switch -> ${kind.fullLabel} failed: package not installed")
                }
                return@launch
            }
            val apk = appInfo.sourceDir
            val uid = appInfo.uid
            val script = "printf '%s %u %s' '$pkg' $uid '$apk' > /proc/kernelsu_manager"
            val (rc, out) = runRootCommand(script, 20)
            if (rc == 0) {
                runRootCommand("am force-stop '$pkg'", 10)
            }
            withContext(Dispatchers.Main) {
                hotSwitchBusy = false
                if (rc == 0) {
                    hotSwitchError = null
                    addLog("Hot switch -> ${kind.fullLabel} (rc=0)")
                    val intent = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        runCatching { startActivity(intent) }
                            .onFailure { addLog("Failed to open ${kind.fullLabel}: $it") }
                    } else {
                        addLog("Manager launch entry not found: $pkg")
                    }
                } else {
                    hotSwitchError = "Switch failed: rc=$rc ${out.take(120)}"
                    addLog("Hot switch -> ${kind.fullLabel} failed rc=$rc ${out.take(120)}")
                }
            }
        }
    }

    private fun resolveDeviceName(): String = JailbreakCore.resolveDeviceName()

    private fun refreshStaticStateAsync() {
        lifecycleScope.launch(Dispatchers.IO) {
            val raw = getSELinuxStatusRaw()
            withContext(Dispatchers.Main) {
                selinuxText = mapSelinux(raw)
                selinuxPermissive = selinuxText.equals("Permissive", ignoreCase = true)
            }
        }
    }

    private fun getSELinuxStatusRaw(): String = try {
        val process = ProcessBuilder("getenforce").start()
        val stdout = process.inputStream.bufferedReader().readText().trim()
        val stderr = process.errorStream.bufferedReader().readText().trim()
        val ok = process.waitFor() == 0
        if (ok) {
            when (stdout) {
                "Enforcing", "Permissive", "Disabled" -> stdout
                else -> "Unknown"
            }
        } else if (stderr.endsWith("Permission denied")) {
            "Enforcing"
        } else {
            "Unknown"
        }
    } catch (t: Throwable) {
        "Unknown"
    }

    private fun mapSelinux(raw: String): String = when (raw) {
        "Enforcing" -> "Enforcing"
        "Permissive" -> "Permissive"
        "Disabled" -> "Disabled"
        else -> "Unknown"
    }

    private fun shizukuEnv(): Array<String> = arrayOf(
        "PATH=/system/bin:/system/xbin:/data/adb/ksu/bin",
        "HOME=/"
    )

    private fun launchManager() {
        val targetPkg = resolveInstalledManagerPkg(manager)
        val intent = packageManager.getLaunchIntentForPackage(targetPkg)
            ?: packageManager.getLaunchIntentForPackage(manager.pkg)
        if (intent == null) {
            addLog("Manager launch entry not found: $targetPkg")
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        runCatching { startActivity(intent) }
            .onFailure { addLog("Failed to open manager: $it") }
    }

    private fun runFlow() {
        if (!RunLock.tryAcquire()) {
            Toast.makeText(this, "已有越狱任务在运行", Toast.LENGTH_SHORT).show()
            return
        }
        busy = true
        val softReboot = softRebootAfter
        val startedAt = System.currentTimeMillis()
        val activeLabel = if (resolveInstalledManagerPkg(manager) == ManagerKind.NEXT_SPOOFED.pkg) {
            ManagerKind.NEXT_SPOOFED.fullLabel
        } else {
            manager.fullLabel
        }
        HistoryStore.start(applicationContext, activeLabel, exploit.fullLabel, kernelVersion, deviceModel)
        lifecycleScope.launch(Dispatchers.IO) {
            var success = false
            var exit = -1
            try {
                if (exploit == ExploitKind.UNSUPPORTED) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            applicationContext,
                            "Unsupported device/kernel: $kernelVersion",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                if (PrivService.pick() == null) {
                    addLog("No privileged backend (Stellar/Shizuku) running")
                    return@launch
                }
                val (alreadyActive, _) = readDeviceState()
                val targetPkg = resolveInstalledManagerPkg(manager)
                if (!targetPkg.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                    addLog("Invalid manager package name: $targetPkg")
                    return@launch
                }
                val p = PrivService.newProcess(
                    arrayOf("sh", "-c",
                        "[ -n \"$(pm path '$targetPkg' 2>/dev/null)\" ] || [ -n \"$(pm path '${manager.pkg}' 2>/dev/null)\" ] || [ -x /data/adb/ksu/bin/ksud ] || [ -x /data/adb/ksud ]"),
                    shizukuEnv(), "/"
                )
                val ksudPreflight = try {
                    p.waitFor()
                } finally {
                    p.close()
                }
                if (ksudPreflight != 0) {
                    addLog("Manager not installed: $targetPkg. Install the manager APK first; ksud is executed from its APK, no bundled copy.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "KernelSU Manager app not installed!\nPlease install KernelSU or KernelSU Next first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                val nativeDir = applicationInfo.nativeLibraryDir
                addLog("Starting ${exploit.fullLabel}")
                val cmd = when (exploit) {
                    ExploitKind.CVE61821 -> arrayOf(
                        if (isPlantCore61821())
                            if (BuildConfig.VIVO_IQOO_SUPPORT &&
                                BuildConfig.VIVO_61821_SUPPORT
                            ) "$nativeDir/libk61821p.so"
                            else "$nativeDir/libk61821x.so"
                        else
                            "$nativeDir/libk61821.so",
                        "run"
                    )
                    ExploitKind.CVE61269 -> arrayOf("$nativeDir/libk61269.so", "run")
                    ExploitKind.CVE61238 -> arrayOf("$nativeDir/libk61238.so", "run")
                    ExploitKind.CVE61230 -> arrayOf("$nativeDir/libk61230.so", "run")
                    ExploitKind.CVE61258 -> arrayOf(
                        if (BuildConfig.VIVO_IQOO_SUPPORT)
                            "$nativeDir/libk61258.so"
                        else
                            "$nativeDir/libk61258x.so",
                        "run"
                    )
                    ExploitKind.CVE61223 -> arrayOf("$nativeDir/libk61223.so", "run")
                    ExploitKind.CVE66118 -> arrayOf("$nativeDir/libk66118.so", "run")
                    ExploitKind.CVE66127 -> arrayOf("$nativeDir/libk66127.so", "run")
                    ExploitKind.CVE6689 -> arrayOf("$nativeDir/libk6689.so", "run")
                    ExploitKind.CVE61 -> arrayOf("$nativeDir/libk61.so", "run")
                    ExploitKind.CVE43499_FUTEX -> arrayOf(
                        JailbreakCore.runnerLib(nativeDir, "CVE43499_FUTEX", kernelVersion) ?: "$nativeDir/libghostlock.so",
                        "run"
                    )
                    ExploitKind.UNSUPPORTED -> null
                } ?: run {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            applicationContext,
                            "Unsupported device/kernel: $kernelVersion",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Verify and log dynamic profile details
                val activeProfile = DynamicProfileManager.lookup(kernelVersion)
                if (activeProfile != null) {
                    addLog("[profile] Active profile: ${activeProfile.model}")
                    addLog("[profile] Engine: ${activeProfile.engine} | Mask/Route: ${activeProfile.siteMask}")
                    if (activeProfile.selinuxState != "0x0") addLog("[profile] selinux_state: ${activeProfile.selinuxState}")
                    if (activeProfile.timerSite != "0x0") addLog("[profile] timer_site: ${activeProfile.timerSite}")
                } else {
                    addLog("[profile] Active profile: Built-in GKI/OEM profile for $kernelVersion")
                }

                val rc = runStreaming(cmd, nativeDir)
                exit = rc
                addLog("exploit exit=$rc")
                val (active, se) = readDeviceState()
                success = rc == 0 && active
                withContext(Dispatchers.Main) {
                    ksuActive = active
                    selinuxText = se
                }
                if (rc == 0 && active) {
                    markRunThisBoot()
                }
                addLog(if (active) "KernelSU module loaded" else "KernelSU module not loaded")
                if (rc == 0 && active && !softReboot) {
                    withContext(Dispatchers.Main) { launchManager() }
                }
                if (softReboot && rc == 0 && active) {
                    val delayMin = softRebootDelayMin

                    softRebootJob?.cancel()
                    softRebootJob = lifecycleScope.launch(Dispatchers.IO) {
                        if (delayMin > 0) {
                            addLog("Jailbreak complete; restarting zygote in ${softRebootDelayLabel(delayMin)}")
                            delay(delayMin * 60_000L)
                        } else {
                            addLog("Jailbreak complete; restarting zygote")
                        }
                        val rebootRc = runCatching {
                            val p = PrivService.newProcess(
                                arrayOf("su", "-c", "setprop ctl.restart zygote"),
                                shizukuEnv(), "/"
                            )
                            try {
                                p.waitFor()
                            } finally {
                                p.close()
                            }
                        }.getOrDefault(-1)
                        if (rebootRc != 0) addLog("Soft reboot failed rc=$rebootRc")
                    }
                }
            } catch (t: Throwable) {
                addLog("ERROR: $t")
            } finally {
                HistoryStore.finish(success, exit, System.currentTimeMillis() - startedAt)
                RunLock.release()
                withContext(Dispatchers.Main) { busy = false }
            }
        }
    }

    private fun runStreaming(cmd: Array<String>, dir: String): Int {
        val targetPkg = resolveInstalledManagerPkg(manager)
        val cfg = JailbreakCore.EnvConfig(
            kind = exploit.name,
            release = kernelVersion,
            managerKind = manager.name,
            antiRootGuard = antiRootGuard,
            unloadGuard = unloadGuard,
            susfsSupport = susfsSupport,
            vfsSupport = vfsSupport,
            universal = dynamicSwitching && supportsUniversal(),
            chaseRetry = chaseRetry,
            customManagerPkg = targetPkg,
            safeModeModules = safeModeModules
        )
        val env = JailbreakCore.buildEnv(applicationContext, cfg, dir)
        return JailbreakCore.runStreaming(cmd, env, dir) { addLog(it) }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderReceivedListener(binderListener)
        Stellar.removeRequestPermissionResultListener(stellarPermissionListener)
        Stellar.removeBinderReceivedListener(stellarBinderListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        refreshProfileState()
    }

    private fun refreshProfileState() {
        dynamicProfileCount = DynamicProfileManager.profileCount()
        val newKind = JailbreakCore.kindFor(kernelVersion)?.let {
            runCatching { ExploitKind.valueOf(it) }.getOrNull()
        } ?: ExploitKind.UNSUPPORTED
        if (exploit != newKind) {
            exploit = newKind
            addLog("[profile] Active exploit engine updated to ${exploit.fullLabel}")
        }
    }

    private enum class AppTab(val icon: ImageVector) {
        HOME(Icons.Filled.Home),
        DEVICES(Icons.Filled.Smartphone),
        LOGS(Icons.Filled.Notes),
        HISTORY(Icons.Filled.History),
        ABOUT(Icons.Filled.Info);

        fun label(lang: AppLanguage): String = when (this) {
            HOME -> AppStrings.tabHome(lang)
            DEVICES -> AppStrings.tabDevices(lang)
            LOGS -> AppStrings.tabLogs(lang)
            HISTORY -> AppStrings.tabHistory(lang)
            ABOUT -> AppStrings.tabAbout(lang)
        }
    }

    private enum class ExploitKind(val fullLabel: String) {
        CVE61821("CVE-2026-98260-6.18"),
        CVE61269("CVE-2026-98260-6.12.69"),
        CVE61238("CVE-2026-98260-6.12.38"),
        CVE61230("CVE-2026-98260-6.12.30"),
        CVE61258("CVE-2026-98260-6.12.58"),
        CVE61223("CVE-2026-98260-vivo-6.12.23"),
        CVE66118("CVE-2026-98260-6.6.118"),
        CVE66127("CVE-2026-98260-vivo-6.6.127"),
        CVE6689("CVE-2026-98260-6.6.89"),
        CVE61("CVE-2026-98260-6.1"),
        CVE43499_FUTEX("CVE-2026-43499-GhostLock"),
        UNSUPPORTED("Unsupported Device")
    }

    private enum class ManagerKind(val fullLabel: String, val pkg: String, val manager: String) {
        KERNELSU("KernelSU", "me.weishu.kernelsu", "kernelsu"),
        FINASU("FinaSU", "me.yuki.fina", "finasu"),
        NEXT("KernelSU Next", "com.rifsxd.ksunext", "next"),
        NEXT_SPOOFED("KernelSU Next (Spoofed)", "kqvdie.zeykvw.hpbcyd", "next_spoofed"),
        BAKASU("BakaSU", "com.resukisu.resukisu", "bakasu"),
        SUKISU("SukiSU Ultra", "com.sukisu.ultra", "sukisu"),
        DYNAMIC("Dynamic Switching", "me.weishu.kernelsu", "kernelsu");

        fun displayLabel(lang: AppLanguage): String = when (this) {
            NEXT_SPOOFED -> when (lang) {
                AppLanguage.ZH -> "KernelSU Next (伪装版)"
                AppLanguage.RU -> "KernelSU Next (Маскированный)"
                AppLanguage.PT -> "KernelSU Next (Camuflado)"
                else -> "KernelSU Next (Spoofed)"
            }
            DYNAMIC -> when (lang) {
                AppLanguage.ZH -> "动态切换"
                AppLanguage.RU -> "Динамическое переключение"
                AppLanguage.PT -> "Troca Dinâmica"
                else -> "Dynamic Switching"
            }
            else -> fullLabel
        }
    }

    private fun isPackageInstalled(pkg: String): Boolean {
        if (!pkg.matches(Regex("^[a-zA-Z0-9._]+$"))) return false
        if (runCatching { packageManager.getPackageInfo(pkg, 0) }.isSuccess) return true
        return runCatching {
            val (rc, out) = runRootCommand("pm path '$pkg' 2>/dev/null", 5)
            rc == 0 && out.contains("package:")
        }.getOrDefault(false)
    }

    private fun resolveInstalledManagerPkg(kind: ManagerKind): String {
        if (kind == ManagerKind.NEXT || kind == ManagerKind.NEXT_SPOOFED) {
            if (customSpoofedPkg.isNotBlank() && isPackageInstalled(customSpoofedPkg)) {
                return customSpoofedPkg
            }
            if (isPackageInstalled(kind.pkg)) return kind.pkg
            val other = if (kind == ManagerKind.NEXT) ManagerKind.NEXT_SPOOFED.pkg else ManagerKind.NEXT.pkg
            if (isPackageInstalled(other)) return other

            val detected = JailbreakCore.findSpoofedNextPackage(this) { cmd -> runRootCommand(cmd, 5) }
            if (!detected.isNullOrEmpty()) {
                customSpoofedPkg = detected
                getSharedPreferences("pyb110", MODE_PRIVATE).edit().putString("custom_spoofed_pkg", detected).apply()
                return detected
            }
            return kind.pkg
        }
        return kind.pkg
    }

    private fun isVivoV2505a(): Boolean =
        (exploit == ExploitKind.CVE61223 || exploit == ExploitKind.CVE61258) &&
            kernelVersion.contains("abogki")

    /** The OPPO anti-root actions are meaningless on the Xiaomi-port kernels. */
    private fun isXiaomiPortKernel(): Boolean =
        kernelVersion == "6.12.69-android16-6-g586bfab1b9c5-abogki536749445-4k" ||
            kernelVersion == "6.18.21-android17-5-g284297031c53-abogki555938958-4k" ||
            kernelVersion == "6.6.118-android15-8-g0908354dac1b-ab16143355-4k" ||
            exploit == ExploitKind.CVE61230 ||
            Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
            Build.BRAND.equals("poco", ignoreCase = true) ||
            Build.BRAND.equals("redmi", ignoreCase = true)

    private fun isMotorolaDevice(): Boolean =
        Build.MANUFACTURER.equals("motorola", ignoreCase = true) ||
            Build.BRAND.equals("motorola", ignoreCase = true) ||
            exploit == ExploitKind.CVE6689

    private fun hidesOplusOnlyOptions(): Boolean =
        isVivoV2505a() || isXiaomiPortKernel() || isMotorolaDevice()

    /** OriginOS 7 builds load the KSU LKM through the hooks_defend gate. */
    private fun isGateKernel(): Boolean =
        JailbreakCore.isGateKernel(exploit.name, kernelVersion)

    private fun supportsUniversal(): Boolean = JailbreakCore.supportsUniversal(exploit.name)

    private fun supportsSusfs(): Boolean =
        JailbreakCore.supportsSusfs(exploit.name, isGateKernel())

    private fun isPlantCore61821(): Boolean = JailbreakCore.isPlantCore61821(kernelVersion)

    /** The 6.18.21 standalone chain has its own retry, not the page chase. */
    private fun supportsChaseRetry(): Boolean =
        JailbreakCore.supportsChaseRetry(exploit.name, kernelVersion)

    private fun defaultChaseRetry(): Boolean = JailbreakCore.defaultChaseRetry(exploit.name)

    private fun managerChoices(): List<ManagerKind> =
        if (supportsUniversal()) ManagerKind.entries
        else ManagerKind.entries.filter { it != ManagerKind.DYNAMIC }

    private fun hotManagerChoices(): List<ManagerKind> =
        ManagerKind.entries.filter { it != ManagerKind.DYNAMIC }

    companion object {
        private const val REQ_SHIZUKU = 1001
        private const val REQ_NOTIFICATIONS = 1002
    }
}
