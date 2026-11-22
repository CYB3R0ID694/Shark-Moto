import java.io.FileInputStream
import java.util.Properties

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) FileInputStream(keystorePropsFile).use { load(it) }
}
val rawTgToken = (keystoreProps["tgBotToken"] as? String) ?: ""
val tgChatId = (keystoreProps["tgChatId"] as? String) ?: "-1004312274608"
val xorKey = 0x5a
val obfuscatedToken = rawTgToken.map { (it.code xor xorKey).toString() }.joinToString(",")

/* vivo/iQOO support is build-time switchable: the sources and the kernel
 * table stay in the tree, but the shipped APK can drop those devices and
 * the vivo-only native payloads.  Build the full package with
 * -PsharkVivo=true. */
val vivoIqoo = (findProperty("sharkVivo") ?: "true").toString().toBoolean()
/* vivo's 6.18.21 (PD2606) chain can also be dropped on its own; build the
 * "full minus PD2606" package with -PsharkVivo=true -PsharkVivo61821=false. */
val vivo61821 = (findProperty("sharkVivo61821") ?: "true").toString().toBoolean()

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "com.shark.neo"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }
    buildToolsVersion = "37.0.0"
    defaultConfig {
        applicationId = "com.shark.neo"
        minSdk = 33
        targetSdk = 37
        versionCode = 106
        versionName = "2.5.4-Test"
        ndk {
            abiFilters.add("arm64-v8a")
        }
        buildConfigField("boolean", "VIVO_IQOO_SUPPORT", vivoIqoo.toString())
        buildConfigField("boolean", "VIVO_61821_SUPPORT", vivo61821.toString())
        buildConfigField("String", "TG_TOKEN_OBFUSCATED", "\"$obfuscatedToken\"")
        buildConfigField("String", "TG_CHAT_ID", "\"$tgChatId\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            if (!vivoIqoo) {
                excludes += setOf(
                    "lib/arm64-v8a/libk61223.so",
                    "lib/arm64-v8a/libk66127.so",
                    "lib/arm64-v8a/libk61258.so",
                    "lib/arm64-v8a/libk61821p.so"
                )
            } else {
                excludes += "lib/arm64-v8a/libk61258x.so"
                if (vivo61821)
                    excludes += "lib/arm64-v8a/libk61821x.so"
                else
                    excludes += "lib/arm64-v8a/libk61821p.so"
            }
            keepDebugSymbols.add("**/libk61821.so")
            keepDebugSymbols.add("**/libksud_universal.so")
            keepDebugSymbols.add("**/libk61269.so")
            keepDebugSymbols.add("**/libk61223.so")
            keepDebugSymbols.add("**/libk61258.so")
            keepDebugSymbols.add("**/libk61238.so")
            keepDebugSymbols.add("**/libk66118.so")
            keepDebugSymbols.add("**/libk66127.so")
            keepDebugSymbols.add("**/libsusfs_guard_lkm.so")
            keepDebugSymbols.add("**/libsusfs_guard_lkm_612.so")
            keepDebugSymbols.add("**/libhybridmount.so")
        }
    }
}


aboutLibraries {
    library {
        duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
        duplicationRule = com.mikepenz.aboutlibraries.plugin.DuplicateRule.SIMPLE
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("io.github.itoseo:coui:0.0.1-alpha7")
    implementation("com.mikepenz:aboutlibraries-core:15.2.0")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:aidl:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("com.github.roro2239:Stellar-API:1.0.3")
}
