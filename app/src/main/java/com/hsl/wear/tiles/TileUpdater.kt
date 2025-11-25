package com.hsl.wear.tiles

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun requestTileUpdate() {
        try {
            android.util.Log.d("TileUpdater", "Tile update request called (relying on automatic refresh)")
            // Note: Manual tile updates are restricted on API 34+
            // We rely on the freshness interval (5 seconds) for automatic updates
            // The tile will refresh itself based on the interval
        } catch (e: Exception) {
            android.util.Log.e("TileUpdater", "Error: ${e.message}", e)
        }
    }
}
