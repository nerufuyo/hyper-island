package com.nerufuyo.hyperisland.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A named, user-created "when to mute islands" rule - the iOS Focus-modes-style
 * generalization of what used to be three separate single-instance settings (manual
 * pause, one schedule, one focus-mode app list). Any number of profiles can exist;
 * islands are muted whenever at least one enabled profile's trigger is currently true.
 *
 * triggerType is one of TRIGGER_MANUAL / TRIGGER_SCHEDULE / TRIGGER_APP_FOREGROUND
 * (plain String, not a Room-enum TypeConverter, to keep this table the same
 * primitives-only shape as IslandHistoryEntry).
 */
@Entity(tableName = "mute_profiles")
data class MuteProfile(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean = true,
    val triggerType: String = TRIGGER_MANUAL,
    val scheduleStartMinutes: Int = 22 * 60,
    val scheduleEndMinutes: Int = 7 * 60,
    val triggerApps: String = "", // comma-joined package names, only used by TRIGGER_APP_FOREGROUND
    // iOS Focus's "Allowed Notifications": apps that still show islands even while this
    // profile is actively muting. Comma-joined package names, empty = allows nothing through.
    val priorityApps: String = "",
    // Bluetooth MAC address, only used by TRIGGER_BLUETOOTH (e.g. car/headset connected).
    val triggerBluetoothAddress: String = "",
    // Geofence center + radius, only used by TRIGGER_LOCATION. Captured via "Use current
    // location" (no map picker - that needs a Google Maps API key this app doesn't have).
    val triggerLatitude: Double = 0.0,
    val triggerLongitude: Double = 0.0,
    val triggerRadiusMeters: Float = 150f,
    val triggerLocationName: String = ""
) {
    companion object {
        const val TRIGGER_MANUAL = "MANUAL"
        const val TRIGGER_SCHEDULE = "SCHEDULE"
        const val TRIGGER_APP_FOREGROUND = "APP_FOREGROUND"
        const val TRIGGER_BLUETOOTH = "BLUETOOTH"
        const val TRIGGER_LOCATION = "LOCATION"
    }
}
