package com.nerufuyo.hyperisland.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.nerufuyo.hyperisland.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Quick Settings tile that toggles the existing "mute islands now" setting
 * (AppPreferences.setDndModeEnabled) - the same flag the manual switch in
 * DndSettingsScreen uses. NotificationReaderService already collects that
 * flow live, so flipping it here is enough; no other wiring needed.
 */
class IslandDndTileService : TileService() {

    private var job = Job()
    private var scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var preferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        job = Job()
        scope = CoroutineScope(Dispatchers.Main + job)
        scope.launch {
            preferences.isDndModeEnabledFlow.collect { isDndEnabled ->
                updateTile(isDndEnabled)
            }
        }
    }

    override fun onStopListening() {
        job.cancel()
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val nowMuted = tile.state != Tile.STATE_ACTIVE
        scope.launch { preferences.setDndModeEnabled(nowMuted) }
    }

    private fun updateTile(isDndEnabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isDndEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Mute Islands"
        tile.updateTile()
    }
}
