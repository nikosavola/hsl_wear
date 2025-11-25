package com.hsl.wear.tiles

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.wear.tiles.TileService
import com.hsl.wear.data.repository.TransitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Transparent activity that handles tile button actions
 */
@AndroidEntryPoint
class TileActionReceiver : ComponentActivity() {

    @Inject
    lateinit var transitRepository: TransitRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent?.getStringExtra(EXTRA_ACTION)
        android.util.Log.d("TileActionReceiver", "=== TileActionReceiver.onCreate ===")
        android.util.Log.d("TileActionReceiver", "Received action: $action")
        android.util.Log.d("TileActionReceiver", "Intent extras: ${intent?.extras}")

        when (action) {
            ACTION_REFRESH -> {
                android.util.Log.d("TileActionReceiver", "Refresh button tapped - tile will auto-refresh within 60s")
                // Note: TileService.getUpdater() is restricted on SDK 35+
                // The tile will auto-refresh based on its freshness interval (min 60s)
                finish()
            }
            ACTION_NEXT_LEG -> {
                scope.launch {
                    val result = transitRepository.advanceToNextLeg()
                    result.onSuccess {
                        android.util.Log.d("TileActionReceiver", "Advanced to next leg, requesting tile update")
                        requestTileUpdate()
                    }.onFailure {
                        android.util.Log.e("TileActionReceiver", "Failed to advance leg: ${it.message}")
                    }
                    finish()
                }
            }
            ACTION_PREV_LEG -> {
                scope.launch {
                    val result = transitRepository.moveToPreviousLeg()
                    result.onSuccess {
                        android.util.Log.d("TileActionReceiver", "Moved to previous leg, requesting tile update")
                        requestTileUpdate()
                    }.onFailure {
                        android.util.Log.e("TileActionReceiver", "Failed to move leg: ${it.message}")
                    }
                    finish()
                }
            }
            else -> finish()
        }
    }

    private fun requestTileUpdate() {
        try {
            // Request tile update - works when user-initiated even on API 34+
            TileService.getUpdater(this)
                .requestUpdate(CurrentLegTileService::class.java)
            android.util.Log.d("TileActionReceiver", "Tile update requested successfully")
        } catch (e: Exception) {
            android.util.Log.e("TileActionReceiver", "Failed to request tile update: ${e.message}", e)
        }
    }

    companion object {
        const val EXTRA_ACTION = "action"
        const val ACTION_REFRESH = "refresh"
        const val ACTION_NEXT_LEG = "next_leg"
        const val ACTION_PREV_LEG = "prev_leg"

        fun createIntent(action: String): Intent {
            return Intent().apply {
                putExtra(EXTRA_ACTION, action)
            }
        }
    }
}
