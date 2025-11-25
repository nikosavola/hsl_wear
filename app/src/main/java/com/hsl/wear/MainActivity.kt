package com.hsl.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hsl.wear.navigation.AppNavigation
import com.hsl.wear.ui.theme.HslWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Triple: destination, legIndex, timestamp (for forcing renavigation)
    private var intentData by mutableStateOf(Triple<String?, Int?, Long>(null, null, 0L))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on when app is in foreground
        setShowWhenLocked(true)

        // Process initial intent
        processIntent(intent)

        setContent {
            HslWearTheme {
                HSLWearApp(
                    tileDestination = intentData.first,
                    tileLegIndex = intentData.second,
                    navigationKey = intentData.third
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Process new intent and trigger recomposition
        processIntent(intent)
    }

    private fun processIntent(intent: android.content.Intent?) {
        val destination = intent?.getStringExtra("destination")
        val legIndex = intent?.getIntExtra("leg_index", -1)?.takeIf { it >= 0 }
        // Use timestamp to ensure navigation happens even if same destination/leg
        intentData = Triple(destination, legIndex, System.currentTimeMillis())
    }
}

@Composable
fun HSLWearApp(
    tileDestination: String? = null,
    tileLegIndex: Int? = null,
    navigationKey: Long = 0L
) {
    // Note: Scaffold with TimeText is now in each individual screen
    AppNavigation(
        tileDestination = tileDestination,
        tileLegIndex = tileLegIndex,
        navigationKey = navigationKey
    )
}
