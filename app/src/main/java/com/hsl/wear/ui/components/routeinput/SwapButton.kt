package com.hsl.wear.ui.components.routeinput

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapButton(
    onSwapLocations: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            try {
                onSwapLocations()
            } catch (e: Exception) {
                android.util.Log.e("SwapButton", "Error in swap button: ${e.message}", e)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .height(32.dp)
            .semantics { contentDescription = "Swap start and destination" },
        colors = ButtonDefaults.filledTonalButtonColors(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Swap",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}