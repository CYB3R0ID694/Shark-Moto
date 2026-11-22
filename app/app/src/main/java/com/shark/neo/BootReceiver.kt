package com.shark.neo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences("pyb110", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("auto_jailbreak", false)) return
        runCatching {
            context.startForegroundService(Intent(context, AutoJailbreakService::class.java))
        }
    }
}
