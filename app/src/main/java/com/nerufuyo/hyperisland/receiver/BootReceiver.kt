package com.nerufuyo.hyperisland.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.util.Log
import com.nerufuyo.hyperisland.service.NotificationReaderService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nerufuyo.hyperisland.data.db.AppDatabase

class BootReceiver : BroadcastReceiver() {

    companion object {
        private var lastToggleTime = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        
        // 1. Determine Importance
        val isMajor = action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                action == "android.intent.action.QUICKBOOT_POWERON"

        val isMinor = action == Intent.ACTION_USER_UNLOCKED ||
                action == Intent.ACTION_USER_PRESENT ||
                action == "android.intent.action.USER_SWITCHED"

        val isTest = action == "com.nerufuyo.hyperisland.ACTION_TEST_MIGRATION"

        if (!isMajor && !isMinor && !isTest) return

        Log.d("HyperIsland", "Trigger event detected: $action")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 2. Migration Logic (Major or Test only)
                if (isMajor || isTest) {
                    Log.d("HyperIsland", "Major trigger: Performing database migration.")
                    AppDatabase.performMigration(context) { progress ->
                        Log.d("HyperIsland", "Migration progress: $progress%")
                    }
                    // Trigger AppPreferences initialization to run SharedPreferences migrations
                    com.nerufuyo.hyperisland.data.AppPreferences(context)
                }
            } catch (e: Exception) {
                Log.e("HyperIsland", "Error during migration in BootReceiver", e)
            } finally {
                // 3. Re-bind Logic
                withContext(Dispatchers.Main) {
                    try {
                        if (isMajor) {
                            val now = System.currentTimeMillis()
                            if (now - lastToggleTime > 5000) {
                                lastToggleTime = now
                                Log.d("HyperIsland", "Major trigger: Toggling NLS component state.")
                                toggleNotificationListener(context)
                            } else {
                                Log.d("HyperIsland", "Major trigger: Cooldown active, skipping toggle.")
                            }
                        } else if (isMinor) {
                            Log.d("HyperIsland", "Minor trigger: Requesting official re-bind.")
                            requestRebind(context)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun requestRebind(context: Context) {
        try {
            val component = ComponentName(context, NotificationReaderService::class.java)
            NotificationListenerService.requestRebind(component)
        } catch (e: Exception) {
            Log.e("HyperIsland", "Failed to request official re-bind", e)
        }
    }

    private fun toggleNotificationListener(context: Context) {
        val pm = context.packageManager
        val componentName = ComponentName(context, NotificationReaderService::class.java)

        // Disable
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // Enable
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}