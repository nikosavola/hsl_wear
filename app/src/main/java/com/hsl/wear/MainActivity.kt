package com.hsl.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.hsl.wear.navigation.AppNavigation
import com.hsl.wear.ui.theme.HslWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on when app is in foreground
        setShowWhenLocked(true)

        setContent {
            HslWearTheme {
                HSLWearApp()
            }
        }
    }
}

@Composable
fun HSLWearApp() {
    // Note: Scaffold with TimeText is now in each individual screen
    AppNavigation()
}
