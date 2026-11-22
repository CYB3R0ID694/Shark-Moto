package com.shark.kty

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class SharkTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val (active, _) = runCatching { JailbreakCore.readDeviceState() }.getOrDefault(false to "")

        if (!active) {
            val serviceIntent = Intent(this, AutoJailbreakService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runCatching { startForegroundService(serviceIntent) }
            } else {
                runCatching { startService(serviceIntent) }
            }
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            runCatching { startActivityAndCollapse(pendingIntent) }
                .onFailure { runCatching { startActivityAndCollapse(mainIntent) } }
        } else {
            runCatching { startActivityAndCollapse(mainIntent) }
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = getSharedPreferences("pyb110", MODE_PRIVATE)
        val lang = AppLanguage.fromCode(prefs.getString("app_language", "en"))
        val (active, _) = runCatching { JailbreakCore.readDeviceState() }.getOrDefault(false to "")

        tile.label = "Shark Neo"
        if (active) {
            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = AppStrings.qsTileRootActive(lang)
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = AppStrings.qsTileRootInactive(lang)
            }
        }
        tile.updateTile()
    }
}
