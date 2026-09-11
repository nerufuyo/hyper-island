package com.nerufuyo.hyperisland.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per island actually shown to the user - lets them scroll back and see
 * what popped up, e.g. one they missed. Written right where NotificationReaderService
 * posts an island, so it only ever contains content that was already displayed
 * (same spoiler/blocklist filtering applies - nothing extra is captured).
 */
@Entity(tableName = "island_history")
data class IslandHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
