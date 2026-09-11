package com.nerufuyo.hyperisland.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IslandHistoryDao {

    @Insert
    suspend fun insert(entry: IslandHistoryEntry)

    @Query("SELECT * FROM island_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 200): Flow<List<IslandHistoryEntry>>

    @Query("DELETE FROM island_history")
    suspend fun clear()

    // ponytail: cap growth by trimming anything past MAX_ENTRIES instead of a
    // separate scheduled cleanup job - called right after each insert.
    @Query(
        """DELETE FROM island_history WHERE id NOT IN
           (SELECT id FROM island_history ORDER BY timestamp DESC LIMIT :keep)"""
    )
    suspend fun trimTo(keep: Int)
}
