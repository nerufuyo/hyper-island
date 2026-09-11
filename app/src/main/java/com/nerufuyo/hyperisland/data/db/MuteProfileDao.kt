package com.nerufuyo.hyperisland.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MuteProfileDao {

    @Query("SELECT * FROM mute_profiles")
    fun getAllFlow(): Flow<List<MuteProfile>>

    @Query("SELECT * FROM mute_profiles WHERE id = :id")
    suspend fun getById(id: String): MuteProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: MuteProfile)

    @Query("DELETE FROM mute_profiles WHERE id = :id")
    suspend fun delete(id: String)
}
