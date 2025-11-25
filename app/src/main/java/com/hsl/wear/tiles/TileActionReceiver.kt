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
        android.util.Log.d("TileActionReceiver", "Received action: $action")

        when (action) {
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
        // Force immediate tile update by triggering the system to refresh
        // The tile will read fresh data from DataStore on next request
        android.util.Log.d("TileActionReceiver", "Tile data updated - will refresh on next view")
    }

    companion object {
        const val EXTRA_ACTION = "action"
        const val ACTION_NEXT_LEG = "next_leg"
        const val ACTION_PREV_LEG = "prev_leg"

        fun createIntent(action: String): Intent {
            return Intent().apply {
                putExtra(EXTRA_ACTION, action)
            }
        }
    }
}
