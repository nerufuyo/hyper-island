package com.nerufuyo.hyperisland.util

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Opens the hidden Xiaomi Autostart management screen.
 */
fun openAutoStartSettings(context: Context) {
    try {
        val intent = Intent()
        intent.component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Settings not found", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Opens the Battery Optimization screen for this app.
 */
@SuppressLint("BatteryLife")
fun openBatterySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = "package:${context.packageName}".toUri()
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Settings not found", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Checks if Notification Listener permission is granted.
 */
fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(pkgName)
}

/**
 * Checks if Post Notification permission (Android 13+) is granted.
 */
fun isPostNotificationsEnabled(context: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if Usage Access is granted - needed for Focus Mode to detect the foreground app.
 * There's no runtime permission dialog for this; the user has to grant it in Settings.
 */
fun isUsageAccessGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

/**
 * Opens the system's "Usage access" list screen (there's no way to deep-link straight to
 * this app's own entry, unlike most other special-permission screens).
 */
fun openUsageAccessSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    } catch (_: Exception) {
        Toast.makeText(context, "Settings not found", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Best-effort "what app is the user looking at right now" check for Focus Mode, using the
 * last 10s of usage events. Returns null if Usage Access isn't granted or nothing was found -
 * callers should treat that as "unknown, don't block the island".
 */
fun getForegroundPackageName(context: Context): String? {
    if (!isUsageAccessGranted(context)) return null
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(now - 10_000, now)
        var lastForegroundPackage: String? = null
        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }
        lastForegroundPackage
    } catch (_: Exception) {
        null
    }
}

/**
 * Checks if Bluetooth Connect permission (Android 12+) is granted - needed for
 * Bluetooth-triggered Mute Profiles to see paired device names and connection broadcasts.
 */
fun isBluetoothConnectGranted(context: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.BLUETOOTH_CONNECT
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Paired (bonded) Bluetooth devices as address-to-name pairs, for the profile editor's device
 * picker. Bonded just means "paired at some point", not "connected right now" - that's tracked
 * separately by NotificationReaderService via ACL_CONNECTED/DISCONNECTED broadcasts.
 */
fun getBondedBluetoothDevices(context: Context): List<Pair<String, String>> {
    if (!isBluetoothConnectGranted(context)) return emptyList()
    return try {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        manager.adapter?.bondedDevices?.map { it.address to (it.name ?: it.address) } ?: emptyList()
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

/**
 * Checks if precise location is granted - needed to capture a geofence center for
 * Location-triggered Mute Profiles.
 */
fun isFineLocationGranted(context: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if background location ("Allow all the time") is granted - required for geofence
 * transitions to fire while the app isn't in the foreground, which for a mute trigger is
 * basically always. On Android 11+ the system handles this via a Settings screen rather than
 * the usual inline dialog, but the same RequestPermission() launcher call triggers either.
 */
fun isBackgroundLocationGranted(context: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

/**
 * One-shot "where am I right now" for the profile editor's "Use current location" button.
 * Returns null if fine location isn't granted or the fetch fails/times out - callers should
 * just show an error rather than silently saving (0.0, 0.0).
 */
suspend fun getCurrentLocation(context: Context): Location? {
    if (!isFineLocationGranted(context)) return null
    return try {
        suspendCancellableCoroutine { continuation ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cancellationSource = CancellationTokenSource()
            continuation.invokeOnCancellation { cancellationSource.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationSource.token)
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
        }
    } catch (_: SecurityException) {
        null
    } catch (_: Exception) {
        null
    }
}